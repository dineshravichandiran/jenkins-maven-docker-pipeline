# Jenkins Pipeline: Git, Maven, Poll SCM, SonarQube and Docker

A Jenkins controller that is built entirely from files (Configuration as Code), watches a Git repository by polling it, and runs a
declarative pipeline: Maven build and tests, a SonarQube quality gate, a Docker image, and a push to a registry. Everything runs on
your machine with Docker Compose; pointing the push at Amazon ECR is a parameter change.

| | |
|---|---|
| Built with | Jenkins (LTS, JDK 17), Jenkins Configuration as Code, Job DSL, declarative Pipeline, Maven, JUnit, SonarQube, Docker, Docker Compose |
| Triggering | **Poll SCM** (`H/2 * * * *`): Jenkins checks Git itself, so no webhook or public URL is needed |
| App | the same small Java 17 HTTP service used in [aws-cicd-pipeline](https://github.com/dineshravichandiran/aws-cicd-pipeline) |
| Status | **run end to end on a local Docker stack** (results below); ECR push not run |

## Pipeline
```mermaid
flowchart LR
  git[Git repo] -->|Poll SCM every ~2 min| j[Jenkins]
  j --> b[Build and test: mvn verify, JUnit report]
  b --> s[SonarQube analysis]
  s --> q{Quality gate}
  q -->|pass| d[docker build]
  q -->|fail| x[pipeline aborted]
  d --> r[(Registry: local registry:2 or ECR)]
```

## What is in the repo
| Path | What it does |
|---|---|
| `Jenkinsfile` | the pipeline: stages, Poll SCM trigger, parameters (`REGISTRY`, `RUN_SONAR`), 20-minute timeout, one build at a time |
| `jenkins/Dockerfile`, `plugins.txt` | Jenkins image with a fixed plugin list and the Docker CLI |
| `jenkins/casc.yaml` | admin user, Maven tool, SonarQube server, token credential and the `greeter` job, all defined as code |
| `docker-compose.yml` | Jenkins + SonarQube Community + a local registry |
| `app/` | the Java app, its tests and its Dockerfile |

## Run it
1. `export JENKINS_ADMIN_PASSWORD='choose-one'` (do not commit it), then `docker compose up -d --build`.
2. Open SonarQube at http://localhost:9000, change the default password, create a token, create a quality gate and make it the default, then
   `export SONAR_TOKEN=...` and `docker compose up -d --force-recreate jenkins`. (To skip the gate, run the job with `RUN_SONAR` unticked.)
3. Open Jenkins at http://localhost:8080, run `greeter` once. After that, a new commit on `main` is picked up within about two minutes.
4. To push to ECR instead: set the `REGISTRY` parameter to your ECR repository URI and log the host Docker in with `aws ecr get-login-password`.

## Design notes and trade-offs
- **Poll SCM instead of a webhook.** A webhook needs Jenkins reachable from GitHub. Polling works behind a laptop or firewall at the cost of up to two minutes' delay and some idle checks.
- **Configuration as Code.** A rebuilt controller comes back identical. Secrets (admin password, Sonar token) come from environment variables, never from the repository.
- **Docker socket is mounted into Jenkins.** That is the simple way to build images, but anyone who can run a job can control the host's Docker. Fine for a personal lab; for shared use I would use a separate build agent or a daemonless builder such as Kaniko.
- **Plugins are not version-pinned** in `plugins.txt`, so a rebuild picks up newer ones. Pin them before relying on this.

## What was and was not checked
Run on a local Docker Compose stack (Jenkins, SonarQube Community and a registry):

| Check | Result |
|---|---|
| Controller starts from code only | The `greeter` job and admin user exist on first start, with no setup wizard |
| Maven tests | 6 tests passed (JUnit) |
| SonarQube analysis and quality gate | Build #7: gate **passed** (coverage 84.4%, duplicated lines 0.0%, reliability rating A, security rating A); the gate fails below 80% coverage, above 3% duplication, or when either rating is worse than A |
| Docker image | Built and pushed to the local registry as `localhost:5001/greeter:main-06cc3932` (digest recorded in the build log) |
| Poll SCM trigger | Build #7 was "Started by an SCM change" after a commit to `main`, with no manual click |

Things that went wrong first, and the fixes:
- A wrong plugin name stopped the image build, and the plugin list was copied with the wrong owner so no plugin installed and the job never appeared. Fixed with `COPY --chown` and a check that fails the build if too few plugins were installed.
- macOS already uses port 5000, so the registry moved to 5001.
- `docker.io` has no CLI on current Debian, so the pipeline failed with "docker: not found". The image now installs `docker-cli`.
- `waitForQualityGate` waits for a SonarQube-to-Jenkins webhook, and SonarQube refuses webhooks to private Docker addresses by default. The build hung. The scanner now waits for the gate itself (`sonar.qualitygate.wait=true`).

**Not done:**
- No push to Amazon ECR was tried; the registry here is a local `registry:2` container.
- The failure path of the gate (a build blocked by a failing gate) was not demonstrated in Jenkins.
- Plugins are not version-pinned, and the Jenkins home volume was not backed up or restored.
