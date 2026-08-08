import de.stefan_oltmann.kim.Kim;
import de.stefan_oltmann.kim.format.MediaMetadata;
import de.stefan_oltmann.kim.input.ByteReader;
import de.stefan_oltmann.kim.input.JvmInputStreamByteReader;
import de.stefan_oltmann.kim.jvm.KimJvm;
import de.stefan_oltmann.kim.model.MetadataUpdate;
import de.stefan_oltmann.kim.output.ByteArrayByteWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        File testFile = new File("testphoto.jpg");

        MediaMetadata imageMetadata = KimJvm.readMetadata(testFile);

        System.out.println(imageMetadata);

        try (FileInputStream inputStream = new FileInputStream(testFile)) {

            ByteReader byteReader =
                new JvmInputStreamByteReader(inputStream, testFile.length());

            ByteArrayByteWriter byteWriter = new ByteArrayByteWriter();

            MetadataUpdate update = new MetadataUpdate.TakenDate(System.currentTimeMillis());

            Kim.update(byteReader, byteWriter, update);

            byte[] updatedBytes = byteWriter.toByteArray();

            try (FileOutputStream fos = new FileOutputStream("testphoto_mod.jpg")) {

                fos.write(updatedBytes);
            }
        }
    }
}
