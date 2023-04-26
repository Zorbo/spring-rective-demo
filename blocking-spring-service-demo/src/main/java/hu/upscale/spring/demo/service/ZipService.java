package hu.upscale.spring.demo.service;

import hu.upscale.spring.demo.exception.CompressionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author László Zoltán
 */
@Slf4j
@Service
public class ZipService {

    private static final int KB_IN_BYTES = 1_024;

    public byte[] compress(byte[] data) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(data);
            deflater.finish();

            byte[] buffer = new byte[KB_IN_BYTES];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }

            byte[] output = outputStream.toByteArray();

            log.debug("Zip compression original data length: {} Kb", data.length / KB_IN_BYTES);
            log.debug("Zip compression compressed data length: {} Kb", output.length / KB_IN_BYTES);

            return output;
        } catch (IOException e) {
            throw new CompressionException("Failed to compress data", e);
        }
    }

    public byte[] decompress(byte[] data) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            Inflater inflater = new Inflater();
            inflater.setInput(data);

            byte[] buffer = new byte[KB_IN_BYTES];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }

            byte[] output = outputStream.toByteArray();

            log.debug("Zip decompression compressed data length: {} Kb", data.length / KB_IN_BYTES);
            log.debug("Zip decompression decompressed data length: {} Kb", output.length / KB_IN_BYTES);

            return output;
        } catch (IOException | DataFormatException e) {
            throw new CompressionException("Failed to decompress data", e);
        }
    }

}
