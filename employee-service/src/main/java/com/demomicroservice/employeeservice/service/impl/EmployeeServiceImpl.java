package com.demomicroservice.employeeservice.service.impl;

import com.demomicroservice.employeeservice.dto.DepartmentDto;
import com.demomicroservice.employeeservice.dto.EmployeeDto;
import com.demomicroservice.employeeservice.dto.Response.ApiResponseDto;
import com.demomicroservice.employeeservice.entity.Employee;
import com.demomicroservice.employeeservice.repository.EmployeeRepository;
import com.demomicroservice.employeeservice.service.EmployeeService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final WebClient webClient;


    public EmployeeServiceImpl(EmployeeRepository employeeRepository, WebClient webClient) {
        this.employeeRepository = employeeRepository;
        this.webClient = webClient;
    }

    @Override
    public EmployeeDto saveEmployee(EmployeeDto employeeDto) {
        Employee employee = new Employee(
                employeeDto.getId(),
                employeeDto.getFirstname(),
                employeeDto.getLastname(),
                employeeDto.getEmail(),
                employeeDto.getDepartmentCode()
        );
       Employee saveEmployee = employeeRepository.save(employee);

       EmployeeDto  saveEmployeeDto = new EmployeeDto(
               saveEmployee.getId(),
               saveEmployee.getFirstname(),
               saveEmployee.getLastname(),
               saveEmployee.getEmail(),
               saveEmployee.getDepartmentCode()
       );
        return saveEmployeeDto;
    }


    //@Retry(name="${spring.application.name}",fallbackMethod = "getDefaultDepartment")
    @CircuitBreaker(name="${spring.application.name}",fallbackMethod = "getDefaultDepartment")
    @Override
    public ApiResponseDto getEmployeeById(Long id) {
        LOGGER.info("inside getEmployeeById method");
        Employee employee = employeeRepository.findById(id).get();

        /**Micro-Services Communication By WebClient*/
        DepartmentDto departmentDto = webClient.get()
                .uri("http://localhost:8083/api/v1/department/"+employee.getDepartmentCode())
                .retrieve()
                .bodyToMono(DepartmentDto.class)
                .block();

        EmployeeDto employeeDto = new EmployeeDto(
                employee.getId(),
                employee.getFirstname(),
                employee.getLastname(),
                employee.getEmail(),
                employee.getDepartmentCode()
        );

        ApiResponseDto apiResponseDto = new ApiResponseDto();
        apiResponseDto.setEmployeeDto(employeeDto);
        apiResponseDto.setDepartmentDto(departmentDto);

        return apiResponseDto;
    }

    public ApiResponseDto getDefaultDepartment(Long id, Exception exception) {
        LOGGER.info("inside getDefaultDepartment method");
        Employee employee = employeeRepository.findById(id).get();

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setDepartmentName("DefaultDepartment");
        departmentDto.setDepartmentCode("D0008");
        departmentDto.setDepartmentDescription("Development Department");

        EmployeeDto employeeDto = new EmployeeDto(
                employee.getId(),
                employee.getFirstname(),
                employee.getLastname(),
                employee.getEmail(),
                employee.getDepartmentCode()
        );

        ApiResponseDto apiResponseDto = new ApiResponseDto();
        apiResponseDto.setEmployeeDto(employeeDto);
        apiResponseDto.setDepartmentDto(departmentDto);

        return apiResponseDto;
    }


}
