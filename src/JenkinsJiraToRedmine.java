import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class JenkinsJiraToRedmine {
    private static final String JIRA_USERNAME = "aimotivetest";
    private static final String JIRA_PASSWORD = "SHUFbcqWrYmwUz";
    private static final String JIRA_BASE_URL = "https://issues.jenkins.io/rest/api/2/";
    private static final String JQL = "search?jql=project=JENKINS%20AND%20status%20in%20(Open,%20\"In%20Progress\",%20Reopened)%20AND%20component%20=%20core";
    private static final String CSV_FILE_PATH = "jenkins_issues.csv";

    public static void main(String[] args) {
        try{
            // Set up the Jira API URL
            URL url = new URL(JIRA_BASE_URL + JQL);

            // Set up the HTTP connection and authentication
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String auth = JIRA_USERNAME + ":" + JIRA_PASSWORD;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestMethod("GET");

            // Read the response from the Jira server
            InputStream inputStream = conn.getInputStream();
            StringBuilder responseBuilder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                responseBuilder.append(new String(buffer, 0, bytesRead));
            }
            String response = responseBuilder.toString();

            // Parse the JSON response and extract the issue data
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray issuesArray = jsonResponse.getJSONArray("issues");

            // Write the issues to a CSV file
            BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH));
            writer.write("\"Project\",\"Issue type\",\"Status\",\"Priority\",\"Summary\",\"Creator\",\"Assignee\",\"Category\",\"Environment\",\"Created\",\"Due date\",\"Estimated time\",\"Related issues\",\"Private\"\n");
            for (int i = 0; i < issuesArray.length(); i++) {
                JSONObject issue = issuesArray.getJSONObject(i);
                String project = "";
                String tracker = "";
                String status = "";
                String priority = "";
                String subject = "";
                String author = "";
                String assignee = "";
                String target_version = "";
                String start_date = "";
                if (!issue.getJSONObject("fields").isNull("project")){
                    project = "\""+issue.getJSONObject("fields").getJSONObject("project").getString("name")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("issuetype")){
                    tracker = "\""+issue.getJSONObject("fields").getJSONObject("issuetype").getString("name")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("status")){
                    status = "\""+issue.getJSONObject("fields").getJSONObject("status").getString("name")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("priority")){
                    priority = "\""+issue.getJSONObject("fields").getJSONObject("priority").getString("name")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("summary")){
                    subject = issue.getJSONObject("fields").getString("summary");
                    subject = subject.replace("\"","\\\"");
                    subject = "\""+subject.replace(",","\\,")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("creator")){
                    author = "\""+issue.getJSONObject("fields").getJSONObject("creator").getString("name")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("assignee")){
                    assignee = "\""+issue.getJSONObject("fields").getJSONObject("assignee").getString("name")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("environment")){
                    target_version  = "\""+issue.getJSONObject("fields").getString("environment")+"\"";
                }
                if (!issue.getJSONObject("fields").isNull("created")){
                    start_date = "\""+issue.getJSONObject("fields").getString("created")+"\"";
                }
                // Write the issue data to the CSV file
                writer.write(project+","+tracker+","+status+","+priority+","+subject+","+author+","+assignee+",,"+target_version+","+start_date+",,,,\"No\"\n");
            }
            writer.close();
            System.out.println("Jenkins Jira issues written to CSV file: " + CSV_FILE_PATH);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
