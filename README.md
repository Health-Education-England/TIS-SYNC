# TIS-SYNC

This Service takes the current cron jobs from the TCS service, running them from this service instead.

For more description, see [Confluence Sync Service Description](https://hee-tis.atlassian.net/wiki/spaces/NTCS/pages/1263271954/Sync+Service)

## Run jobs out of schedule

### Job Execution Order
![](jobDependency.svg)

### Summary of what to run:

| Failed/Out-of-date Job                   |          Managed Bean Name          |                            Method Name |                           Things that need to be done after this has been started |
| ---------------------------------------- | :---------------------------------: | -------------------------------------: | --------------------------------------------------------------------------------: |
| **PersonOwnerRebuildJob**                |        PersonOwnerRebuildJob        |                  personOwnerRebuildJob |                                                                              none |
| **PersonPlacementEmployingBodyTrustJob** |   PersonPlacementEmployingBodyJob   | doPersonPlacementEmployingBodyFullSync | Re-run **PersonPlacementTrainingBodyTrustJob** and **PersonElasticSearchSyncJob** |
| **PersonPlacementTrainingBodyTrustJob**  | PersonPlacementTrainingBodyTrustJob |    PersonPlacementTrainingBodyFullSync |                        **PersonElasticSearchSyncJob** (assuming only this failed) |
| **PostEmployingBodyTrustJob**            |      PostEmployingBodyTrustJob      |         PostEmployingBodyTrustFullSync |                                               Re-run **PostTrainingBodyTrustJob** |
| **PostTrainingBodyTrustJob**             |      PostTrainingBodyTrustJob       |          PostTrainingBodyTrustFullSync |                                                  none (assuming only this failed) |
| **PersonRecordStatusJob**  | PersonRecordStatusJob |    personRecordStatusJob |                        **PersonElasticSearchSyncJob** (assuming only this failed) |
| **PersonElasticSearchSyncJob**           |       PersonElasticSearchJob        |                personElasticSearchSync |                                                                              none |

### Run jobs on Buttons Page
1. If you want to run jobs on server, go to page https://\<host IP here\>/sync/.
2. Click **Get Status** to view statuses of all the jobs. If can't get any response from the page, please refer to [permission to run jobs on the page](#permission)
3. Then click **Run job** buttons or **Run All Jobs** button to trigger the jobs you want.
#### <span id="permission">Permission to run jobs on the page:</span>
1. "Machine User" role is used to view the statuses and click the buttons. If you don't have this role, you won't get any response after clicking any buttons.<br>
2. "Machine User" role is hidden in **UserManagement** page, so need to add this role to the database you need on the fly.<br>
3. SQL to add "Machine User" Role:
```
INSERT INTO UserRole(userName, roleName) 
VALUES (YOUR_USER_NAME, "Machine User");
```