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
  const jobName = event.srcElement.id;
  const url = window.location.origin + window.location.pathname + "api/jobs/status";
  console.log(url);
  fetch(url)
    .then(response => response.json())
    .then(status => {
        console.log(status);
        document.getElementById("personOwnerRebuildJob_status").innerHTML = status.personOwnerRebuildJob;
    });
}
