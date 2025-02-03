Feature: Get job details
  Get the job details from Scheduling

  Scenario: Get basic job info
    Given job ref is "autoGUI_ob_1"
    When GetJob is called
    Then return code should be "0"

  Scenario: Job does not exist
    Given job ref is "does_not_exist"
    When GetJob is called
    Then return code should be "1"

  Scenario: Test with a file
    Given request file is "get_job.request.xml"
    When GetJob is called
    Then response data should be "get_job.response.xml"
