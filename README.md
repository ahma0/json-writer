## 사용법

아래와 같이 `JsonIOData`를 상속받는 객체를 만듭니다.

```
@Getter
@Setter
@NoArgsConstructor
public class ProcessData implements JsonIOData {
    private String processName;
    private Long JvmPID;
    private Map<String, ChromeInfo> info = new ConcurrentHashMap<>();

    @Builder
    public ProcessData(String processName, Long jvmPID) {
        this.processName = processName;
        this.JvmPID = jvmPID;
    }

    @Override
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public void addInfo(ProcessDto dto) {
        String threadName = Thread.currentThread().getName();
        if (existThreadName(threadName)) {
            info.remove(threadName);
        }
        info.put(threadName, TestInfo.of(dto));
    }

    @Override
    public void removeInfo() {
        info.remove(Thread.currentThread().getName());
    }

...

```

이 후 다른 코드에서 아래와 같이 사용하면 됩니다.

```
JsonWriter.createAndLoadJsonFile(object, PortData.class, PortData::of);
JsonWriter.removeDataInJsonFile(processName, PortData.class);
```
