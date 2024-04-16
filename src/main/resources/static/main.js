function runJob() {
  const jobName = event.srcElement.id;
  const url = window.location.origin + window.location.pathname + "api/job/" + jobName;

  const requestParams = {
    method : "PUT",
    body : "",
    headers : { "Content-Type" : "application/json" }
    };

  fetch(url, requestParams)
    .then(response => response.json())
    .then(responseJson => updateStatusAfterTriggerJob(responseJson, jobName));
}

// After clicking any button of "Run job", set status from the response
function updateStatusAfterTriggerJob(response, jobName) {
  if (response.status) {
    console.log(response);
    const statusElement = document.getElementById(jobName + "_status");
    statusElement.innerHTML = response.status;
    statusElement.classList.add("running-text");
  }
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
    })
    .then(responseJson => updateStatusAfterTriggerJob(responseJson, jobName));
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
        });
        // set status and tooltip for revalCurrmentPMJob
        setStatusForRevalCurrentFieldJobs();
    });
}

function setStatusForRevalCurrentFieldJobs() {
  const elements = [document.getElementById("revalCurrentPmJob_status"), document.getElementById("revalCurrentPlacementJob_status")];
  // these jobs send personIds to tcs and trigger
  elements.forEach((el) => {
      el.innerHTML = "Unknown";
      el.setAttribute("title", "Please check tcs/reval/rabbitMQ if this job is currently running");
      el.classList.remove("running-text");
  })

}

function fetchSysProfile() {
  var profile = "default";
  const url = window.location.origin + window.location.pathname + "api/sys/profile";

  fetch(url)
    .then(response => response.text())
    .then(data => {
      profile = data;
      if (profile.indexOf("nimdta") === -1) {
        document.getElementById("reval-panel").classList.remove("hidden");
      }
    });
}

function registerListeners() {
  fetchSysProfile();
  document.getElementById("personOwnerRebuildJob").addEventListener("click", runJob);
  document.getElementById("personPlacementEmployingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("personPlacementTrainingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("postEmployingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("postTrainingBodyTrustJob").addEventListener("click", runJob);
  document.getElementById("personElasticSearchSyncJob").addEventListener("click", runJob);
  document.getElementById("personRecordStatusJob").addEventListener("click", runPersonStatusSyncJob);
  document.getElementById("postFundingSyncJob").addEventListener("click", runJob);
  document.getElementById("runAllJobs").addEventListener("click", runAllJobs);
  document.getElementById("getStatus").addEventListener("click", getStatus);
  document.getElementById("revalCurrentPmJob").addEventListener("click", runJob);
  document.getElementById("revalCurrentPlacementJob").addEventListener("click", runJob);
}

window.addEventListener("load", registerListeners);
