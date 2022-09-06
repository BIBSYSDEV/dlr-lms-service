# dlr-lms-service
This repository contains components for DLR integration in Canvas.

For now LMS service is used to launch DLR in Canvas only.

Other LMS integrations should be implemented in this service.

## lms-canvas integration
Lambda for launching DLR in Canvas. Lambda should redirect to DLR/iframe, or handle request. ```LtiLaunchHandler``` in ```lti-tool-provider```
decides if it will redirect, or return an HTML/ XML based on ```serviceIdentifier``` in the request. 

## Required configuration
The following configuration must be made in the Secrets Manager in the AWS account
where it will be deployed:
Key: ```knownConsumerKey```

This secret value should be fetched by CanvasLaunchHandler, requires configuration path. 

Request from canvas has following parameters:

```json
{
  "path": "lms/canvas/v1/{serviceIdentifier}",
  "queryStringParameters": {
    "oauth_consumer_key": "knownCustomerKey",
    "lti_message_type": "basic-lti-launch-request",
    "lti_version": "LTI-1p0",
    "resource_link_id": "resource_link_id",
    "launch_presentation_return_url": "https://example.com"
  }
}
```
Valid values for identifier are following:
-  combined
-  site
-  embedExternalTool
-  embedContentEditor
-  empty space

Query string parameters are default parameters, except oauth_consumer_key, which should be configured 
in secrets manager. Request without query string parameters is also a valid request.