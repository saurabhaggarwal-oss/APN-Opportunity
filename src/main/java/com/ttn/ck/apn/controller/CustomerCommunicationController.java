package com.ttn.ck.apn.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for APN Opportunity data operations.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET  /customer-communication/getAll}              — Listing of customers data</li>
 *   <li>{@code POST /customer-communication/add                  — Add New Customer</li>
 *   <li>{@code GET  /customer-communication/update               — Update customer by id</li>
 *   <li>{@code GET  /customer-communication/delete               — Delete customer by id</li>
 * </ul>
 */

@Slf4j
@RestController
@RequestMapping("/customer-communication")
@RequiredArgsConstructor
public class CustomerCommunicationController {

}
