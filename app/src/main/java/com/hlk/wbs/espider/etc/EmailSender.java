package com.hlk.wbs.espider.etc;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * <p>
 * <b>发送邮件 EmailSender 可以当作一个工具类来用，直接调用方法 发邮件是耗时操作，记得起个新线程</b>
 * </p>
 * <h2>例子:</h2>
 *
 * <pre class="prettyprint">
 * EmailSender sender = new EmailSender();
 * // 设置服务器地址和端口
 * sender.setProperties(&quot;smtp.126.com&quot;, &quot;25&quot;);
 * // 分别设置发件人，邮件标题和文本内容
 * sender.setMessage(&quot;******@126.com&quot;, &quot;Email send test&quot;, &quot;This is from JavaMail !&quot;);
 * // 设置收件人
 * sender.setReceiver(new String[] { &quot;******@gmail.com&quot;, &quot;******@qq.com&quot; });
 * // 添加附件
 * sender.addAttachment(&quot;/sdcard/debug.txt&quot;);
 * // 发送邮件
 * sender.sendEmail(&quot;smtp.126.com&quot;, &quot;******@126.com&quot;, &quot;密码******&quot;);
 * </pre>
 */
public class EmailSender {
	private Properties properties;
	private Session session;
	private Message message;
	private MimeMultipart multipart;

	public EmailSender() {
		super();
		this.properties = new Properties();
	}

	public void setProperties(String host, String post) {
		// 地址
		this.properties.put("mail.smtp.host", host);
		// 端口号
		this.properties.put("mail.smtp.post", post);
		// 是否验证
		this.properties.put("mail.smtp.auth", true);
		this.session = Session.getInstance(properties);
		this.message = new MimeMessage(session);
		this.multipart = new MimeMultipart("mixed");
	}

	/**
	 * 设置收件人
	 *
	 * @param receiver receiver
	 * @throws MessagingException
	 */
	public void setReceiver(String[] receiver) throws MessagingException {
		Address[] address = new InternetAddress[receiver.length];
		for (int i = 0; i < receiver.length; i++) {
			address[i] = new InternetAddress(receiver[i]);
		}
		this.message.setRecipients(Message.RecipientType.TO, address);
	}

	/**
	 * 设置邮件
	 *
	 * @param from
	 *            来源
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void setMessage(String from, String title, String content)
			throws AddressException,

			MessagingException {
		this.message.setFrom(new InternetAddress(from));
		this.message.setSubject(title);
		// 纯文本的话用setText()就行，不过有附件就显示不出来内容了
		MimeBodyPart textBody = new MimeBodyPart();
		textBody.setContent(content, "text/html;charset=gbk");
		this.multipart.addBodyPart(textBody);
	}

	/**
	 * 添加附件
	 *
	 * @param filePath
	 *            文件路径
	 * @throws MessagingException
	 */
	public void addAttachment(String filePath) throws MessagingException {
		FileDataSource fileDataSource = new FileDataSource(new File(filePath));
		DataHandler dataHandler = new DataHandler(fileDataSource);
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDataHandler(dataHandler);
		mimeBodyPart.setFileName(fileDataSource.getName());
		this.multipart.addBodyPart(mimeBodyPart);
	}

	/**
	 * 发送邮件
	 *
	 * @param host
	 *            地址
	 * @param account
	 *            账户名
	 * @param pwd
	 *            密码
	 * @throws MessagingException
	 */
	public void sendEmail(String host, String account, String pwd)
			throws MessagingException {
		// 发送时间
		this.message.setSentDate(new Date());
		// 发送的内容，文本和附件
		this.message.setContent(this.multipart);
		this.message.saveChanges();
		// 创建邮件发送对象，并指定其使用SMTP协议发送邮件
		Transport transport = session.getTransport("smtp");
		// 登录邮箱
		transport.connect(host, account, pwd);
		// 发送邮件
		transport.sendMessage(message, message.getAllRecipients());
		// 关闭连接
		transport.close();
	}
}
