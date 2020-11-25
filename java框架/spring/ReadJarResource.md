### 如何读取依赖jar中的资源文件
1. 读取jar中文件需要使用流的形式
```java
PathMatchingResourcePatternResolver resourcePatternResolver =
                new PathMatchingResourcePatternResolver(
                    APPlication.class.getClassLoader());
            Resource[] resources =
                resourcePatternResolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX+privateKeyPath);
//            List<String> privateKeyList =
//                Files.readAllLines(
//                    Paths.get(resources[0].getURI()),
//                    StandardCharsets.UTF_8)
//                    .stream()
//                    .map(base64-> new String((Base64.getDecoder().decode(base64)),StandardCharsets.UTF_8)
//                    ).collect(Collectors.toList());
            InputStream inputStream = resources[0].getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            //读取
            List<String> privateKeyList = reader
                .lines();
                // .map(base64-> new String((Base64.getDecoder().decode(base64)),StandardCharsets.UTF_8))
                // .collect(Collectors.toList());
```