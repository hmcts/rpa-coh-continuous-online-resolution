continuous-online-hearing


To run in docker
```bash
./gradlew clean assemble;
docker-compose up --build;
```

TODO Notes on the current state of this branch
Work was started on this branch. However, it wasn't finished because big changes needed to be merged into master, right before the app is going live. The work was therefore delayed until it's safe to push stuff into master.

Issues that still need to be resolved -

1. A plugin to required to pass secrets into Kubernetes. However, this plugin requires a springboot version of 2.1.x or higher, while the app uses version 2.0.8. Upgrading the version causes issues with the testing configuration, but it needs to happen for Kubernetes to work.

2. I noticed that the Jenkins build wasn't running the steps associated with installCharts(), despite the fact that the flag is there in Jenkinsfile_CNP. Maybe it needs to be merged into master once before it'll work, but I don't think so. Also, try moving installCharts() to before the "after functional:aat ..." code in Jenkinsfile_CNP.
