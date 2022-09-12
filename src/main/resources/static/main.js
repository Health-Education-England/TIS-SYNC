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

function handleErrors(response) {
  if (!response.ok) {
     return Promise.reject(response.json());
  }
  return response.json();
}

function runPersonStatusSyncJob() {
  const jobName = event.srcElement.id;
  const url = window.location.origin + window.location.pathname + "api/job/" + jobName;

  var promptMsg = "Please input the magic date override: \n" +
    "  keep it blank (repeat the overnight job) \n" +
    "  ANY (update all records) \n" +
    "  date in format YYYY-MM-DD (update for a particular date)\n" +
    "  NONE (to use the existing magic)";
  var date = prompt(promptMsg);
  if (date === null) { // user clicks cancel button
    return;
  }
  if (!validateDateFormat(date)) {
    window.alert("Date is not correct!");
    return;
  }

  const requestParams = {
    method : "PUT",
    body : JSON.stringify({ "dateOverride" : date }),
    headers : { "Content-Type" : "application/json" }
    };

  fetch(url, requestParams)
    .then(handleErrors)
    .catch(errPromise => {
      errPromise.then(errObj => window.alert(errObj.error));
    });
}

function validateDateFormat(param) {
  var reg = /(19|20)\d\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])/;
  if (param === "" || param.toUpperCase() === "ANY"
    || param.toUpperCase() === "NONE" || param.match(reg)) {
    return true;
  }
  return false;
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
  document.getElementById("personRecordStatusJob").addEventListener("click", runPersonStatusSyncJob);
  document.getElementById("runAllJobs").addEventListener("click", runAllJobs);
  document.getElementById("getStatus").addEventListener("click", getStatus);
}

window.addEventListener("load", registerListeners);
