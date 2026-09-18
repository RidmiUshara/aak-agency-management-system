package lk.aak.agency.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrCodeService {

    private static final int QR_SIZE_PX = 300;

    public byte[] generatePng(String content) {
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    content, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Could not generate the QR code image.", exception);
        }
    }
}
