package io.keyko.nevermined.models;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.nevermined.models.gateway.ComputeStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ComputeStatusTest {

    private static String COMPUTE_STATUS_SAMPLE = "src/test/resources/examples/compute-status.json";
    private static String COMPUTE_STATUS_CONTENT;

    @BeforeClass
    public static void setUp() throws Exception {
        COMPUTE_STATUS_CONTENT = new String(Files.readAllBytes(Paths.get(COMPUTE_STATUS_SAMPLE)));
    }
    @Test
    public void fromJsonTest() throws Exception {
        ComputeStatus computeStatus = ComputeStatus.fromJSON(new TypeReference<>() {}, COMPUTE_STATUS_CONTENT);

        assertEquals("did:nv:7ce18efb179b65a1ca4b1598ad4d1fb4107c4fe51336e2871d3f7ae208873fd4",
                computeStatus.did.getDid());
        assertEquals("2020-09-18T12:24:33+00:00Z", computeStatus.startedAt);
        assertEquals("2020-09-18T12:24:50+00:00Z", computeStatus.finishedAt);
        assertEquals("Succeeded", computeStatus.status);
        assertEquals(3, computeStatus.pods.size());

        assertEquals("2020-09-18T12:24:49+00:00Z", computeStatus.pods.get(0).finishedAt);
        assertEquals("2020-09-18T12:24:44+00:00Z", computeStatus.pods.get(0).startedAt);
        assertEquals("publishing", computeStatus.pods.get(0).podName);
        assertEquals("Succeeded", computeStatus.pods.get(0).status);
    }
}
