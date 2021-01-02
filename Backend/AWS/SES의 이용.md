<h1>AWS SES Java에서 이용하기</h1>

<h2>AWS SES 도입 이전</h2>

* AWS SES를 알기 전에는 Java에서 이메일을 보내기 위해 `javax.mail` 라이브러리를 사용했다.   
  이 라이브러리로 이메일을 보내는 코드는 아래와 같다.
```java
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {
    public static void sendEmail(EmailContent emailContent, String recepeint, String senderEmail, String password, boolean isDebugAllowed) throws MessagingException {

        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", 465);
        properties.put("mail.smtp.starttls.enable", "true");

        properties.put("mail.debug", "" + isDebugAllowed);

        properties.put("defaultEncoding", "utf-8");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, password);
            }
        });

        String content = EmailContent.SimpleTemplate(emailContent);

        MimeMessage message = new MimeMessage(session);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setText(content, "UTF-8", "html");
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepeint));
        message.setSubject(emailContent.getTitle());
        Transport.send(message);
    }
}
```

* 위에서 사용한 `EmailContent`는 내가 작성한 클래스이다. (이메일 형식 유지의 간편함을 위해..)

<hr/>

<h2>AWS SES 도입</h2>

* AWS SES를 도입하기 전에, 위의 코드로 이메일을 전송할 때의 문제점은 __느리다는 것__ 이다.   
  2000ms가 넘는 시간이 소요되었기 때문이다.

<h3>AWS SES</h3>

* `SES(Simple Email Service)`는 Amazon에서 제공하는 비용 효율적이고, 간단한 이메일 플랫폼이다.   
  <a href="https://docs.aws.amazon.com/ses/latest/DeveloperGuide/Welcome.html">SES 소개</a>

* 기존에 도메인이 있다면, 이메일 서버를 구축하기 위해 직접 구축 또는 외주를 맡겨야 하는데, SES를 사용하면   
  이 과정을 모두 AWS가 처리해준다.

<h3>설정 방법</h3>

* 우선, `Domain`에 가서 `Verify a New Domain`을 클릭한다. (기존에 도메인이 있다고 가정)

* 그 후 사용하는 Domain을 등록하면 된다. 여기서 DKIM은 꼭 설정해주자.   
  DKIM을 비활성화하면 SES를 이용해서 전송되는 이메일이 수신자의 스팸함으로 가게될 수도 있다.

* Verification을 안내사항에 따라 완성한다. (~~Route53 쓰면 정말 편하다..~~)

* 그 후 Sandbox를 해제하고, 인증을 받은 후 `Email Address`를 등록한다.   
  인증된 이메일을 통해 이메일이 발송되는 것이다.

* 만약 기존 이메일은 `Gmail`을 사용하며, 도메인은 `abc.com`이라면 SES에서 `Gmail`을 인증하고, 도메인도 인증하면      
  `*.abc.com`의 주소를 발신자로 보이게 이메일을 전송할 수 있다.

<h3>코드</h3>

* S3와 마찬가지로, 아래 코드도 AWS-SDK-JAVA v2로 작성되었다.

* 먼저 Gradle 설정은 아래와 같다.
```gradle
// Other gradle dependencies and configurations..

dependencies {
    implementation platform('software.amazon.awssdk:bom:2.15.0')
    implementation('software.amazon.awssdk:ses')
}
```

* 위 모듈을 사용하여 작성한 코드는 아래와 같다.
```java
import com.banchango.common.dto.BasicMessageResponseDto;
import com.banchango.common.exception.InternalServerErrorException;
import com.banchango.tools.EmailContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

@Component
public class EmailSender {

    private SesClient sesClient;

    @Value("${aws.ses.access_key_id}")
    private String accessKey;

    @Value("${aws.ses.secret_access_key}")
    private String secretAccessKey;

    @PostConstruct
    public void setSesClient() {
        sesClient = SesClient.builder()
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKey, secretAccessKey))
                .region(Region.AP_NORTHEAST_2).build();
    }

    public BasicMessageResponseDto send(String recipient, EmailContent emailContent) {

        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("발신자 이메일"));
            message.setSubject("이메일 제목");
            message.setSender(new InternetAddress("전송자 이메일"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            String content = EmailContent.SimpleTemplate(emailContent);

            Multipart multipart = new MimeMultipart();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setText(content, "UTF-8", "html");
            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            ByteBuffer byteBuffer = ByteBuffer.wrap(outputStream.toByteArray());

            byte[] byteArr = new byte[byteBuffer.remaining()];
            byteBuffer.get(byteArr);

            SdkBytes data = SdkBytes.fromByteArray(byteArr);

            RawMessage rawMessage = RawMessage.builder()
                    .data(data).build();

            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage).destinations(recipient).build();

            sesClient.sendRawEmail(rawEmailRequest);

            return new BasicMessageResponseDto("이메일이 정상적으로 전송되었습니다.");

        } catch(MessagingException | IOException exception) {
            throw new InternalServerErrorException(exception.getMessage());
        }
    }
}
```
<hr/>