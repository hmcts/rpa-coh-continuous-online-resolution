package uk.gov.hmcts.reform.coh.actuate.info;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;

public class BuildInfoTest {

    // private static final String BUILD_INFO = "META-INF/build-info.properties";
    private static final String BUILD_INFO_WITH_BUILD_NO = "META-INF/build-info-with-build-no.properties";


    @Test
    public void shouldAddBuildInfoToBuilder() throws Exception {
        BuildInfo dmBuildInfo = new BuildInfo("name","env","project");

        Info.Builder builder = new Info.Builder();
        dmBuildInfo.contribute(builder);


        Map<String,Object> buildInfo = new HashMap<>();
        buildInfo.put("environment", "env");
        buildInfo.put("project", "project");
        buildInfo.put("name", "name");
        buildInfo.put("version", "unknown");
        buildInfo.put("date", "unknown");
        buildInfo.put("commit", "unknown");
        buildInfo.put("extra", Collections.EMPTY_MAP);

        Map<String,Object> map = new HashMap<>();
        map.put("buildInfo",buildInfo);

        Info info = builder.build();

        assertThat(info.getDetails(), CoreMatchers.equalTo(map));
    }

    @Test
    public void shouldAddBuildInfoToBuilderNullBuildInfo() throws Exception {
        BuildInfo buildInfo = new BuildInfo("name","env","project",null);

        Info.Builder builder = new Info.Builder();
        buildInfo.contribute(builder);

        Map<String,Object> buildInfoMap = new HashMap<>();
        buildInfoMap.put("environment", "env");
        buildInfoMap.put("project", "project");
        buildInfoMap.put("name", "name");
        buildInfoMap.put("version", "unknown");
        buildInfoMap.put("date", "unknown");
        buildInfoMap.put("commit", "unknown");
        buildInfoMap.put("extra", Collections.EMPTY_MAP);

        Map<String,Object> map = new HashMap<>();
        map.put("buildInfo",buildInfoMap);

        Info info = builder.build();

        assertThat(info.getDetails(), CoreMatchers.equalTo(map));
    }

    @Test
    public void shouldAddBuildInfoToBuilderIncludesBuildNumber() throws Exception {
        BuildInfo buildInfo = new BuildInfo("name","env","project",BUILD_INFO_WITH_BUILD_NO);

        Info.Builder builder = new Info.Builder();
        buildInfo.contribute(builder);

        Map<String,Object> buildInfoMap = new HashMap<>();
        buildInfoMap.put("environment", "env");
        buildInfoMap.put("project", "project");
        buildInfoMap.put("name", "name");
        buildInfoMap.put("version", "1.0-42");
        buildInfoMap.put("date", "today");
        buildInfoMap.put("commit", "aaaaaaa");
        buildInfoMap.put("extra", Collections.EMPTY_MAP);

        Map<String,Object> map = new HashMap<>();
        map.put("buildInfo",buildInfoMap);

        Info info = builder.build();

        assertThat(info.getDetails(), CoreMatchers.equalTo(map));
    }


}
