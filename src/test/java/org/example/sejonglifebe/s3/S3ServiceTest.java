package org.example.sejonglifebe.s3;

import org.example.sejonglifebe.common.image.ConvertedImage;
import org.example.sejonglifebe.common.image.ImageConverter;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ImageConverter imageConverter;

    @Test
    @DisplayName("이미지 URL에서 추출한 객체 key로 삭제를 요청한다")
    void deleteImages_usesObjectKeyExtractedFromUploadedImageUrl() {
        // given
        S3Service s3Service = new S3Service(s3Client, "sejong-life", imageConverter);
        given(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .willReturn(DeleteObjectsResponse.builder().build());

        // when
        s3Service.deleteImages(List.of(
                "https://sejong-life.s3.ap-northeast-2.amazonaws.com/12-550e8400-e29b-41d4-a716-446655440000.webp"));

        // then
        ArgumentCaptor<DeleteObjectsRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(s3Client).deleteObjects(requestCaptor.capture());
        assertThat(requestCaptor.getValue().delete().objects())
                .extracting(identifier -> identifier.key())
                .containsExactly("12-550e8400-e29b-41d4-a716-446655440000.webp");
    }

    @Test
    @DisplayName("객체별 삭제 실패가 있으면 예외를 던진다")
    void deleteImages_detectsPerObjectFailureEvenWhenHttpRequestSucceeds() {
        // given
        given(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .willReturn(DeleteObjectsResponse.builder()
                        .errors(S3Error.builder()
                                .key("old.webp")
                                .code("AccessDenied")
                                .build())
                        .build());
        S3Service service = new S3Service(s3Client, "sejong-life", imageConverter);

        // when & then
        assertThatThrownBy(() -> service.deleteImages(List.of("https://sejong-life.s3.amazonaws.com/old.webp")))
                .isInstanceOf(SejongLifeException.class);
    }

    @Test
    @DisplayName("PNG 파일을 WebP로 변환해 업로드한다")
    void uploadImage_convertsActualPngToWebpBeforeSendingToMockS3() throws Exception {
        // given
        // 변환기는 실제로 실행하고, 네트워크 호출만 mock 처리한다.
        S3Service service = new S3Service(s3Client, "sejong-life", new ImageConverter());
        given(s3Client.utilities()).willReturn(S3Utilities.builder().region(Region.AP_NORTHEAST_2).build());
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).willAnswer(invocation -> {
            PutObjectRequest request = invocation.getArgument(0);
            RequestBody body = invocation.getArgument(1);
            byte[] bytes;
            try (InputStream stream = body.contentStreamProvider().newStream()) {
                bytes = stream.readAllBytes();
            }
            assertThat(request.contentType()).isEqualTo("image/webp");
            assertThat(request.key()).startsWith("1-").endsWith(".webp");
            assertThat(new String(bytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("RIFF");
            assertThat(new String(bytes, 8, 4, StandardCharsets.US_ASCII)).isEqualTo("WEBP");
            return PutObjectResponse.builder().build();
        });
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", png);

        // when
        String url = service.uploadImage("1",
                new MockMultipartFile("thumbnail", "photo.png", "image/png", png.toByteArray()));

        // then
        assertThat(url).startsWith("https://sejong-life.s3.ap-northeast-2.amazonaws.com/1-").endsWith(".webp");
    }

    @Test
    @DisplayName("ACL 설정에 실패하면 업로드한 객체를 정리한다")
    void uploadImage_aclFailureDeletesTheUploadedKey() {
        // given
        MockMultipartFile file = new MockMultipartFile("thumbnail", "photo.png", "image/png", new byte[]{1});
        given(imageConverter.convert(file, "png")).willReturn(
                new ConvertedImage(new ByteArrayInputStream(new byte[]{1}), 1, "image/webp", "webp"));
        given(s3Client.putObjectAcl(any(PutObjectAclRequest.class)))
                .willThrow(S3Exception.builder().message("ACL denied").statusCode(403).build());
        given(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .willReturn(DeleteObjectsResponse.builder().build());
        S3Service service = new S3Service(s3Client, "sejong-life", imageConverter);

        // when & then
        assertThatThrownBy(() -> service.uploadImage("1", file))
                .isInstanceOf(SejongLifeException.class)
                .hasMessage(ErrorCode.S3_UPLOAD_FAILED.getErrorMessage());

        ArgumentCaptor<PutObjectRequest> uploaded = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<DeleteObjectsRequest> deleted = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(s3Client).putObject(uploaded.capture(), any(RequestBody.class));
        verify(s3Client).deleteObjects(deleted.capture());
        assertThat(deleted.getValue().delete().objects()).extracting(identifier -> identifier.key())
                .containsExactly(uploaded.getValue().key());
    }
}
