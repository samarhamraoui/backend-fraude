package com.example.backend.Controllers;

import com.example.backend.entities.Flow;
import com.example.backend.services.FlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/flows")
@Tag(name = "Flows API", description = "Endpoints flows")
public class FlowController {
    @Autowired
    private FlowService flowService;
    @Operation(summary = "Get all active flows")
    @GetMapping("/active")
    private ResponseEntity<List<Flow>> getFlowBytype(){
        return ResponseEntity.ok(flowService.getFlowsByStatusAndTypeId(1L, Arrays.asList(1, 6)));
    }

    @Operation(
            summary = "Get flows by flow type and subquery",
            description = "Retrieve flows where `flow_type` equals 6 and `proc.id` is in the subquery based on the provided `idFlow`."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the flows"),
            @ApiResponse(responseCode = "400", description = "Invalid input or request"),
            @ApiResponse(responseCode = "404", description = "No flows found for the given criteria"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-type-and-proc")
    public ResponseEntity<List<Flow>> getFlowsByTypeAndProc(
            @Parameter(description = "The ID of the flow to use in the subquery")
            @RequestParam("idFlow") Long idFlow
    ) {
        List<Flow> flows = flowService.findFlowsByFlowTypeAndProc(idFlow);

        if (flows.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(flows);
    }

}
