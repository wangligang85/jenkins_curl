package leon.code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author wangligang85@163.com on 2018-12-10 18:02
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String INPUT_FILE_NAME = "/Users/leon/httpclient/projects.data";


    public static void main(String[] args) {
        logger.info("-----------start-------------");
        long start = System.currentTimeMillis();
        dealDataInFile();
        long end = System.currentTimeMillis();
        logger.info("-----------end, use:{}s-------------", (end - start)/1000);
    }

    /**
     * 读取文件中的内容并处理
     */
    static void dealDataInFile(){

        try (Stream<String> stream = Files.lines(Paths.get(INPUT_FILE_NAME))) {

                    stream.forEach(line -> {
//                                HttpWorker.getInstance().doCreate(line);
//                                HttpWorker.getInstance().doDisable(line);
//                                HttpWorker.getInstance().doEnable(line);
//                                HttpWorker.getInstance().doBuild(line);
//                                HttpWorker.getInstance().doDelete(line);
                            });
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
    }
}
