set GITHUB_PAT=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
curl -X POST ^
  -H "Accept: application/vnd.github+json" ^
  -H "Authorization: Bearer %GITHUB_PAT%" ^
  https://api.github.com/repos/sreeharinath1/github_actions/dispatches ^
  -d "{ \"event_type\": \"deploy\", \"client_payload\": { \"environment\": \"production\", \"version\": \"v1.2.3\", \"triggered-by\": \"jenkins\" } }"
