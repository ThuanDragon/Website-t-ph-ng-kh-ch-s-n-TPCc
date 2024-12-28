package com.example.hotelmanager.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.Booking;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendConnectEmail(String hotelName, String phone, String email, String ideas, boolean nda) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("tinhnhps29956@fpt.edu.vn");
        message.setSubject("New Connect Request from " + hotelName);
        message.setText("Hotel Name: " + hotelName + "\n" +
                        "Phone: " + phone + "\n" +
                        "Email: " + email + "\n" +
                        "Ideas: " + ideas + "\n" +
                        "NDA: " + nda);
        javaMailSender.send(message);
    }

    public void sendBookingConfirmationEmail(Booking booking) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("tinhnhps29956@fpt.edu.vn");
        message.setSubject("*** Có đơn đặt phòng mới *** - " + booking.getId());

        BigDecimal pricePerNight = booking.getRoom().getPrice();
        long totalNights = booking.getCheckOut().toEpochDay() - booking.getCheckIn().toEpochDay();
        BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(totalNights));

        message.setText("Booking ID: " + booking.getId() + "\n" +
                        "Khách hàng: " + booking.getUser().getUsername() + "\n" +
                        "Room: " + booking.getRoom().getRoomNumber() + "\n" +
                        "Check-in: " + booking.getCheckIn() + "\n" +
                        "Check-out: " + booking.getCheckOut() + "\n" +
                        "Số người: " + booking.getGuests() + "\n" +
                        "Tổng tiền: " + totalPrice + " VND" // Thêm tổng tiền vào email
        );

        javaMailSender.send(message);
    }

    public void sendConfirmationEmail(String toEmail, String subject, String body) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, true);

        javaMailSender.send(message);
    }
}
