package com.example.hotelmanager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.Payment;
import com.example.hotelmanager.repository.PaymentRepository;

@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	
	@Autowired
	public PaymentService(PaymentRepository paymentRepository)	{
		this.paymentRepository = paymentRepository;
	}
	
	public List<Payment> getAllPayments(){
		return paymentRepository.findAll();
	}
	
	public Optional<Payment>	getPaymentById(Long id)	{
		return paymentRepository.findById(id);
	}
	
	public List<Payment> getPaymentByUserId(Long userId)	{
		return paymentRepository.findByUser_Id(userId);
	}
	
	public List<Payment> getPaymentsByBookingId(Long bookingId)	{
		return paymentRepository.findByBooking_Id(bookingId);
	}
	
	public List<Payment> getPaymentsByStatus(String status)	{
		return paymentRepository.findByStatus(status);
	}
	
	public Payment createPayment(Payment payment) {
		//them check thanh toan
		List<Payment> existingPayments = paymentRepository.findByBooking_Id(payment.getBooking().getId());
		if(!existingPayments.isEmpty())	{
			throw new RuntimeException("Booking này đã được thanh toán.");
		}
		
		return paymentRepository.save(payment);
	}
	
	public Payment updatePayment(Payment payment)	{
		if(!paymentRepository.existsById(payment.getId())) {
			throw new RuntimeException("Payment not found");
		}
		return paymentRepository.save(payment);
	}
	
	public void deletePayment(Long id)	{
		 paymentRepository.deleteById(id);
	}
}
