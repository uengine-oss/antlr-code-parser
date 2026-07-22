package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.recovery.repair.FailureEnvelope;
import legacymodernizer.parser.recovery.repair.FailureEnvelopeFactory;

class FailureEnvelopeContractTest {

    @Test
    void canonicalChecksumMatchesParserOwnedV2Fixture() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream input = getClass().getResourceAsStream(
                "/recovery/contracts/failure-envelope-v2.json")) {
            FailureEnvelope envelope = mapper.readValue(input, FailureEnvelope.class);
            assertEquals(envelope.failureEnvelopeHash(),
                    new FailureEnvelopeFactory().checksum(envelope));
        }
    }
}
