
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import kr.co.jirandata.crawler.selenium.core.SeleniumCrawlerDriver;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@link JsonIOData}를 상속·구현한 객체를 .json파일로 저장·삭제합니다.
 *
 * @see JsonIOData
 */
@Log4j2
public class JsonWriter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String JSON_DIR_WINDOW = "windowsPath";
    private static final String JSON_DIR_LINUX = "linuxPath";

    /**
     * json 파일을 작성합니다. {@link JsonIOData}를 상속·구현한 객체만 사용할 수 있습니다.
     *
     * @param processName
     * @param clazz
     * @param creator
     * @param <T>
     * @see JsonIOData
     */
    synchronized public static <T extends JsonIOData> void createAndLoadJsonFile(
            Object object, Class<T> clazz, JsonDataCreator<T> creator
    ) {

        String filePath = getFilePath(object.getProcessName(), clazz);
        T data = null;

        if (!doesJsonFileExists(filePath)) {
            data = creator.create();
            log.info("📁 JSON 파일이 존재하지 않아 새 파일을 생성합니다. 생성된 객체: {}", data.toJson());
        } else {
            try {
                data = loadJsonFile(filePath, clazz);
                log.info("📁 JSON 파일을 클래스 객체로 성공적으로 불러왔습니다. 불러온 객체: {}", data.toJson());
            } catch (IOException | JsonSyntaxException e) {
                log.error("🧨 JSON 파일을 불러오는 중 오류가 발생했습니다.: " + e.getMessage());
                return;
            }

            data.addInfo(object);
        }

        saveToJsonFile(filePath, data);
    }

    /**
     * json 파일 안에 데이터를 삭제합니다. {@link JsonIOData}를 상속·구현한 객체만 사용할 수 있습니다.
     *
     * @param processName
     * @param clazz
     * @param <T>
     * @see JsonIOData
     */
    synchronized public static <T extends JsonIOData> void removeDataInJsonFile(String processName, Class<T> clazz) {
        String filePath = getFilePath(processName, clazz);

        if (!doesJsonFileExists(filePath)) {
            log.info("📁 JSON 파일이 존재하지 않습니다.");
            return;
        }

        try {
            T data = loadJsonFile(filePath, clazz);
            data.removeInfo();
            log.info("📁 수집이 완료되어 파일에서 데이터를 제거하였습니다. -> {}", filePath);
            saveToJsonFile(filePath, data);
        } catch (IOException e) {
            log.error("🧨 JSON 파일 저장 중 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * json 파일을 읽어옵니다.
     *
     * @param processName
     * @param clazz
     * @param <T>
     * @return
     */
    synchronized public static <T extends JsonIOData> T loadJsonData(String processName, Class<T> clazz) {
        String filePath = getFilePath(processName, clazz);
        Path path = Path.of(filePath);

        if (!doesJsonFileExists(path)) {
            log.info("📁 JSON 파일이 존재하지 않습니다.");
            return null;
        }

        T data = null;
        try {
            data = loadJsonFile(filePath, clazz);
        } catch (IOException e) {
            log.error("🧨 JSON 로드 중 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return data;
    }

    private static <T> T loadJsonFile(Path filePath, Class<T> classOfT) throws IOException, JsonSyntaxException {
        return mapper.readValue(filePath.toFile(), classOfT);
    }

    private static boolean doesJsonFileExists(Path filePath) {
        boolean doesExist = false;
        
        try {
            doesExist = Files.exists(filePath);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return doesExist;
    }

    private static @NotNull String getFilePath(String processName, @NotNull Class<?> clazz) {
        String end = getExtension(clazz); //파일 이름 구분

        return new StringBuffer()
                .append(checkAndGetDirPathByOs())
                .append(processName).append(end).toString();
    }

    private static String checkAndGetDirPathByOs() {
        return SystemUtils.IS_OS_WINDOWS ? JSON_DIR_WINDOW : JSON_DIR_LINUX;
    }

    private static void saveToJsonFile(String filePath, Object objectToSave) {
        try (FileWriter writer = new FileWriter(filePath)) {
            String json = mapper.writeValueAsString(objectToSave);
            writer.write(json);
            log.info("📁 파일 저장을 완료하였습니다. -> {}", filePath);
        } catch (IOException e) {
            log.error("🧨 JSON 파일 저장 중 오류가 발생했습니다: {}", e.getMessage());
        }
    }

    private static String getExtension(@NotNull Class<?> clazz) {
        return ".json";
        // return clazz.equals(___.class) ? "_PORT.json" : ".json";
    }

}
