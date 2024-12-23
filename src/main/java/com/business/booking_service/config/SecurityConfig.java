package com.business.booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/bookings/endpoints").permitAll()
                        .requestMatchers("/api/bookings/add").permitAll()
                        .requestMatchers("/api/bookings/check-active").permitAll()
                        .requestMatchers("/api/bookings/direct").permitAll()
                        .requestMatchers("/api/bookings/all").permitAll()
                        .requestMatchers("/api/bookings/{id}").permitAll()
                        .requestMatchers("/api/bookings/{id}/user").permitAll()
                        .requestMatchers("/api/bookings/search").permitAll()
                        .requestMatchers("/api/bookings/update/{id}/status").permitAll()
                        .requestMatchers("/api/bookings/booking_table/update/{bookingId}/status").permitAll()
                        .requestMatchers("/api/bookings/booking_table/update/{bookingId}/status/paying").permitAll()
                        .requestMatchers("/api/bookings/booking_table/update/{bookingId}/status/paymentProcessing").permitAll()
                        .requestMatchers("/api/bookings/history/{userId}").permitAll()
                        .requestMatchers("/api/bookings/booking_table/{bookingId}").permitAll()
                        .requestMatchers("/api/bookings/orders/today").permitAll()
                        .requestMatchers("/api/bookings/orders/range").permitAll()
                        .requestMatchers("/api/bookings/orders/count-tables").permitAll()
                        .requestMatchers("/api/bookings/orders/count-tables/range").permitAll()
                        .requestMatchers("/api/bookings/booking_table/most-booked-tables").permitAll()
                        .requestMatchers("/api/bookings/booking_table/most-booked-tables/range").permitAll()
                        .requestMatchers("/api/bookings/booking_table/delete").permitAll()
                        .requestMatchers("/api/bookings/booking_table/update-table-id").permitAll()
                        .requestMatchers("/api/bookings/booking_table/update-tables").permitAll()
                        .requestMatchers("/api/bookings/{bookingId}/tables").permitAll()

                        .requestMatchers("/api/bookings/bookingTables/{bookingId}").permitAll()
                        .requestMatchers("/api/bookings/booking_table/check-table-used/{tableId}").permitAll()
                        .anyRequest().authenticated()

                );





        return http.build();
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
