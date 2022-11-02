INSERT INTO `ProgrammeMembership` (`uuid`, `personId`, `programmeStartDate`, `programmeEndDate`, `programmeId`)
VALUES
  ('0023f7bc-defa-48b4-8186-5e680e981df4', 1, CURRENT_DATE, DATEADD(MONTH, 1, CURRENT_DATE), 1),
  ('0023f7bc-defa-48b4-8186-5e680e981de4', 2, DATEADD(MONTH, -1, CURRENT_DATE), DATEADD(DAY, -1, CURRENT_DATE), 1),
  ('0023f7bc-defa-48b4-8186-5e680e981dd4', 3, DATEADD(MONTH, -1, CURRENT_DATE), DATEADD(MONTH, 1, CURRENT_DATE), 1);
