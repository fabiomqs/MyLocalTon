package org.ton.liteclient;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ton.executors.liteclient.LiteClientExecutor;
import org.ton.executors.liteclient.LiteClientParser;
import org.ton.executors.liteclient.api.ResultLastBlock;
import org.ton.settings.GenesisNode;
import org.ton.settings.Node;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(ConcurrentTestRunner.class)
public class LiteClientExecutorThreadsTest {

    private static final String CURRENT_DIR = System.getProperty("user.dir");
    private static final String TESTNET_CONFIG_LOCATION = CURRENT_DIR + File.separator + "testnet-global.config.json";

    private LiteClientExecutor liteClientExecutor;
    private Node testNode;

    @Before
    public void executedBeforeEach() throws IOException {
        InputStream TESTNET_CONFIG = IOUtils.toBufferedInputStream(getClass().getResourceAsStream("/testnet-global.config.json"));
        Files.copy(TESTNET_CONFIG, Paths.get(TESTNET_CONFIG_LOCATION), StandardCopyOption.REPLACE_EXISTING);

        liteClientExecutor = new LiteClientExecutor();

        testNode = new GenesisNode();
        testNode.extractBinaries();
        testNode.setNodeGlobalConfigLocation(TESTNET_CONFIG_LOCATION);
    }

    @Test
    @ThreadCount(8)
    public void testLiteClientLastThreads() throws Exception {
        String resultLast = liteClientExecutor.executeLast(testNode);
        assertThat(resultLast).isNotEmpty();
        ResultLastBlock resultLastBlock = LiteClientParser.parseLast(resultLast);
        log.info("testLiteClientLastThreads tonBlockId {}", resultLastBlock);
        assertThat(resultLastBlock).isNotNull();
        String resultShards = liteClientExecutor.executeAllshards(testNode, resultLastBlock);
        log.info("testLiteClientLastThreads resultShards {}", resultShards);
        assertThat(resultShards).isNotEmpty();
        String resultBlock = liteClientExecutor.executeDumpblock(testNode, resultLastBlock);
        assertThat(resultBlock).isNotEmpty();
    }
}