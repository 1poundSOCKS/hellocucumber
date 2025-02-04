Feature: Get job details
  Get the job details from Scheduling

  Scenario: Get basic job info
    Given job ref is "autoGUI_ob_1"
    When APIAgent is called
    Then return code should be "0"

  Scenario: Job does not exist
    Given job ref is "does_not_exist"
    When APIAgent is called
    Then return code should be "1"

  Scenario: Test 'GetJob' with a file
    Given request file is "get_job.request.xml"
    When APIAgent is called
    Then response data should be "get_job.response.xml"

  Scenario: Test 'CancelJob' with a file
    Given request file is "cancel_job.request.xml"
    When APIAgent is called
    Then return code can be a warning
