package com.scavengerhunt.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Contract test for LandmarkProcessorClient
 * 验证与 landmark-processor 微服务的契约一致性
 */
@SpringBootTest
@TestPropertySource(properties = {
    "landmark.processor.url=http://localhost:5002"  // 使用宿主机端口
})
public class LandmarkProcessorClientContractTest {

    @Autowired
    private LandmarkProcessorClient client;

}
