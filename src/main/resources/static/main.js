function runJob() {
  const jobName = event.srcElement.id;
  const url = window.location.origin + window.location.pathname + "api/job/" + jobName;

  const requestParams = {
    method : "PUT",
    body : "",
    headers : { "Content-Type" : "application/json" }
    };

  fetch(url, requestParams)
    .then(response => response.json());
}

function runAllJobs() {
  const url = window.location.origin + window.location.pathname + "api/jobs/";

  const requestParams = {
    method : "PUT",
    body : "",
    headers : { "Content-Type" : "application/json" }
    };

  fetch(url, requestParams)
    .then(response => response.json());
}

function getStatus() {
  const url = window.location.origin + window.location.pathname + "api/jobs/status";
  fetch(url)
    .then(response => response.json())
    .then(status => {
        Object.keys(status).forEach(function(key) {
          var jobStatus = "Not running";
          const statusElement = document.getElementById(key + "_status");

          if (status[key]) {
            jobStatus = "Running";
            statusElement.classList.add("running-text");
          }
          else {
            statusElement.classList.remove("running-text");
          }

          statusElement.innerHTML = jobStatus;
        })
    });
}

function registerListeners() {
  document.getElementById("personOwnerRebuildJob").addEventListener("click", runJob);
  document.getElementById("personPlacementEmployingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("personPlacementTrainingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("postEmployingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("postTrainingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("personElasticSearchSyncJob").addEventListener("click", runJob);
  document.getElementById("personRecordStatusJob").addEventListener("click", runJob);
  document.getElementById("runAllJobs").addEventListener("click", runAllJobs);
  document.getElementById("getStatus").addEventListener("click", getStatus);
}

window.addEventListener("load", registerListeners);
