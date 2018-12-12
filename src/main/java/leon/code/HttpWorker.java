package leon.code;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wangligang85@163.com on 2018-12-11 11:38
 */
public class HttpWorker {
    //并发线程数
    private static final int HTTP_WORKER_THREAD_NUM = 200;
    private final static String username = "jenkins_user";
    private final static String password = "jenkins_password";
    private final static String COMMAND_CREATE = "create";
    private final static String COMMAND_DISABLE = "disable";
    private final static String COMMAND_ENABLE = "enable";
    private final static String COMMAND_BUILD = "build";
    private final static String COMMAND_DELETE = "delete";

    private static AtomicLong counter = new AtomicLong();

    private final static Logger logger = LoggerFactory.getLogger(HttpWorker.class);
    private ExecutorService executor = Executors.newFixedThreadPool(HTTP_WORKER_THREAD_NUM);

    private HttpWorker(){}

    public static final HttpWorker getInstance() {
        return HttpWorkerHolder.INSTANCE;
    }

    public void doCreate(String keeWord){
        executor.submit(new HttpJobTask(COMMAND_CREATE, keeWord));
    }
    public void doDisable(String keeWord){
        executor.submit(new HttpJobTask(COMMAND_DISABLE, keeWord));
    }
    public void doEnable(String keeWord){
        executor.submit(new HttpJobTask(COMMAND_ENABLE, keeWord));
    }
    public void doBuild(String keeWord){
        executor.submit(new HttpJobTask(COMMAND_BUILD, keeWord));
    }
    public void doDelete(String keeWord){
        executor.submit(new HttpJobTask(COMMAND_DELETE, keeWord));
    }

    class HttpJobTask implements Runnable, Serializable {
        String command;
        String projectName;

        HttpJobTask(String command, String projectName) {
            this.command = command;
            this.projectName = projectName;
        }

        public void run() {
            try {
                executeJenkinsTask(command, projectName);
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
    }

    void executeJenkinsTask(String command, String projectName){
        logger.info("do get request  {} : {} ", counter.incrementAndGet(), projectName);
        String url = "";
        switch(command) {
            case COMMAND_CREATE :
                url = "http://jenkins.local/createItem?mode=copy&from=exampleProject&name=" + projectName;
                break;
            case COMMAND_DISABLE :
                url = "http://jenkins.local/job/" + projectName + "/disable";
                break;
            case COMMAND_ENABLE :
                url = "http://jenkins.local/job/" + projectName + "/enable";
                break;
            case COMMAND_BUILD:
                url = "http://jenkins.local/job/" + projectName + "/build";
                break;
            case COMMAND_DELETE:
                url = "http://jenkins.local/job/" + projectName + "/doDelete";
                break;
        }
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            URI uri = URI.create(url);
            HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(username, password));
            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(host, basicAuth);
            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpPost httpPost = new HttpPost(uri);
            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);
            response = httpClient.execute(host, httpPost, localContext);

            // 处理结果
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 302) {
            } else {
                logger.warn(projectName  + "__" + response.getStatusLine().getStatusCode() + "___" + response.getStatusLine().getReasonPhrase());
                logger.warn(JSON.toJSONString(response));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static class HttpWorkerHolder {
        private static final HttpWorker INSTANCE = new HttpWorker();
    }
}


