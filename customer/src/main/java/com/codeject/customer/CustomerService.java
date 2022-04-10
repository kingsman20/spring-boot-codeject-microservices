package com.codeject.customer;

import com.codeject.amqp.RabbitMQMessageProducer;
import com.codeject.clients.fraud.FraudCheckResponse;
import com.codeject.clients.fraud.FraudClient;
import com.codeject.clients.notification.NotificationClient;
import com.codeject.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {

    private final RabbitMQMessageProducer rabbitMQMessageProducer;
    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final NotificationClient notificationClient;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
            // todo: check if email valid
            // todo: check if email not taken
            customerRepository.save(customer);
            customerRepository.saveAndFlush(customer);
            // todo: check if fraudster
        FraudCheckResponse fraudCheckResponse =
                fraudClient.isFraudster(customer.getId());

            if (fraudCheckResponse.isFraudster()) {
                throw new IllegalStateException("fraudster");
            }

        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to Codeject...",
                        customer.getFirstName())
        );
        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );

        }
}