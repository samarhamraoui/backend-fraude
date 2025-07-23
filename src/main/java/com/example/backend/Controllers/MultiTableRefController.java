package com.example.backend.Controllers;

import com.example.backend.dao.GenericRepository;
import com.example.backend.entities.*;
import com.example.backend.entities.dto.DetailsFormDTO;
import com.example.backend.entities.dto.FilterRequest;
import com.example.backend.services.MultiTableRefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/multitableref")
@Tag(name = "Multi Table Reference API", description = "API for fetching references from multiple tables.")
public class MultiTableRefController {
    @Autowired
    private MultiTableRefService multiRefService;
    @Autowired
    private GenericRepository<ListAppelant, Integer> genericRepo;
    @Autowired
    private GenericRepository<ListAppele, Integer> appeleRepo;
    @Autowired
    private GenericRepository<ListCellid, Integer> cellidRepo;
    @Autowired
    private GenericRepository<ListImei, Integer> imeiRepo;
    @Autowired
    private GenericRepository<DetailsListAppelant, Integer> detailsListAppelantRepo;
    @Autowired
    private GenericRepository<DetailsListAppele, Integer> detailsListAppeleRepo;
    @Autowired
    private GenericRepository<DetailImei, Integer> detailsImeiRepo;
    @Autowired
    private GenericRepository<DetailsListCellid, Integer> detailsCellidRepo;

    @Operation(
            summary = "Fetch data by filter type",
            description = "Possible values for {filtre} are: 'offre', 'appelant', 'appele', 'cellid', 'imei', 'type destination', 'plan tarifaire'"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched data."),
            @ApiResponse(responseCode = "404", description = "No matching filter or data found."),
    })
    @PostMapping
    public ResponseEntity<List<?>> fetchData(@RequestBody FilterRequest request) {
        String filtre = request.getFiltre().trim().toLowerCase(); // Normalize input

        List<?> result;
        switch (filtre) {
            case "offre":
                result = multiRefService.getAllOffre();
                break;
            case "appelant":
                result = multiRefService.getAllAppelant();
                break;
            case "appele":
                result = multiRefService.getAllAppele();
                break;
            case "cellid":
                result = multiRefService.getAllCellId();
                break;
            case "imei":
                result = multiRefService.getAllImei();
                break;
            case "type destination":
                result = multiRefService.getAllTypedest();
                break;
            case "plan tarifaire":
                result = multiRefService.getAllPlanTarifaire();
                break;
            default:
                result = Collections.emptyList();
                break;
        }
        return result.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }



    @PostMapping("/called")
    public ResponseEntity<ListAppelant> create(@RequestBody ListAppelant newAppelant) {
        newAppelant.setDateModif(new Timestamp(System.currentTimeMillis()));
        ListAppelant saved = genericRepo.save(newAppelant);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/called/{id}")
    public ResponseEntity<ListAppelant> update(
            @PathVariable Integer id,
            @RequestBody ListAppelant updated
    ) {
        ListAppelant existing = genericRepo.find(ListAppelant.class, id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setNom(updated.getNom());
        existing.setDateModif(new Timestamp(System.currentTimeMillis()));
        ListAppelant merged = genericRepo.update(existing);
        return ResponseEntity.ok(merged);
    }

    @DeleteMapping("/called/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (genericRepo.find(ListAppelant.class, id) == null) {
            return ResponseEntity.notFound().build();
        }
        genericRepo.delete(ListAppelant.class, id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------
    // CALLED (ListAppele) CRUD
    // --------------------------------------------------

    @PostMapping("/caller")
    public ResponseEntity<ListAppele> createAppele(@RequestBody ListAppele newAppele) {
        newAppele.setDateModif(new Timestamp(System.currentTimeMillis()));
        ListAppele saved = appeleRepo.save(newAppele);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/caller/{id}")
    public ResponseEntity<ListAppele> updateAppele(
            @PathVariable Integer id,
            @RequestBody ListAppele updated
    ) {
        ListAppele existing = appeleRepo.find(ListAppele.class, id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setNom(updated.getNom());
        existing.setDateModif(new Timestamp(System.currentTimeMillis()));
        ListAppele merged = appeleRepo.update(existing);
        return ResponseEntity.ok(merged);
    }

    @DeleteMapping("/caller/{id}")
    public ResponseEntity<Void> deleteAppele(@PathVariable Integer id) {
        ListAppele existing = appeleRepo.find(ListAppele.class, id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        appeleRepo.delete(ListAppele.class, id);
        return ResponseEntity.noContent().build();
    }


    // --------------------------------------------------
    // CELLID (ListCellid) CRUD
    // --------------------------------------------------
    @PostMapping("/cellid")
    public ResponseEntity<ListCellid> createCellid(@RequestBody ListCellid newCell) {
        newCell.setDateModif(new Timestamp(System.currentTimeMillis()));
        ListCellid saved = cellidRepo.save(newCell);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/cellid/{id}")
    public ResponseEntity<ListCellid> updateCellid(
            @PathVariable Integer id,
            @RequestBody ListCellid updated
    ) {
        ListCellid existing = cellidRepo.find(ListCellid.class, id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        //existing.setId(updated.getId());
        existing.setDateModif(new Timestamp(System.currentTimeMillis()));
        existing.setNom(updated.getNom());
        ListCellid merged = cellidRepo.update(existing);
        return ResponseEntity.ok(merged);
    }

    @DeleteMapping("/cellid/{id}")
    public ResponseEntity<Void> deleteCellid(@PathVariable Integer id) {
        ListCellid existing = cellidRepo.find(ListCellid.class, id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        cellidRepo.delete(ListCellid.class, id);
        return ResponseEntity.noContent().build();
    }


    // --------------------------------------------------
    // IMEI (ListImei) CRUD
    // --------------------------------------------------
    @PostMapping("/imei")
    public ResponseEntity<ListImei> createImei(@RequestBody ListImei newImei) {
        newImei.setDateModif(new Timestamp(System.currentTimeMillis()));
        ListImei saved = imeiRepo.save(newImei);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/imei/{id}")
    public ResponseEntity<ListImei> updateImei(
            @PathVariable Integer id,
            @RequestBody ListImei updated
    ) {
        ListImei existing = imeiRepo.find(ListImei.class, id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setDateModif(new Timestamp(System.currentTimeMillis()));
        existing.setNom(updated.getNom());
        ListImei merged = imeiRepo.update(existing);
        return ResponseEntity.ok(merged);
    }

    @DeleteMapping("/imei/{id}")
    public ResponseEntity<Void> deleteImei(@PathVariable Integer id) {
        ListImei existing = imeiRepo.find(ListImei.class, id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        imeiRepo.delete(ListImei.class, id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------
    // DETAILS ADD EDIT
    // --------------------------------------------------

    @PostMapping("/details")
    public ResponseEntity<DetailsFormDTO> createOrUpdateDetails(@RequestBody DetailsFormDTO newDetails) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        String type = newDetails.getType().toLowerCase();

        try {
            switch (type) {
                case "appele":
                    if (newDetails.getIdPrincipal() != null) {
                        // Edit existing DetailsListAppele
                        DetailsListAppele existingDetail = detailsListAppeleRepo.find(DetailsListAppele.class, newDetails.getIdDetail());
                        if (existingDetail == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        existingDetail.setHotlistnumber(newDetails.getValue());
                        existingDetail.setDateModif(currentTimestamp);
                        detailsListAppeleRepo.save(existingDetail);
                    } else {
                        // Create new DetailsListAppele
                        ListAppele parentAppele = appeleRepo.find(ListAppele.class, newDetails.getIdPrincipal());
                        if (parentAppele == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        DetailsListAppele calledDetail = new DetailsListAppele();
                        calledDetail.setDateModif(currentTimestamp);
                        calledDetail.setHotlistnumber(newDetails.getValue());
                        calledDetail.setListAppele(parentAppele);
                        detailsListAppeleRepo.save(calledDetail);
                    }
                    break;

                case "appelant":
                    if (newDetails.getIdDetail() != null) {
                        DetailsListAppelant existingDetail = detailsListAppelantRepo.find(DetailsListAppelant.class, newDetails.getIdDetail());
                        if (existingDetail == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        existingDetail.setHotlistnumber(newDetails.getValue());
                        existingDetail.setDateModif(currentTimestamp);
                        detailsListAppelantRepo.save(existingDetail);
                    } else {
                        ListAppelant parentAppelant = genericRepo.find(ListAppelant.class, newDetails.getIdPrincipal());
                        if (parentAppelant == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        DetailsListAppelant callerDetail = new DetailsListAppelant();
                        callerDetail.setDateModif(currentTimestamp);
                        callerDetail.setHotlistnumber(newDetails.getValue());
                        callerDetail.setListAppelant(parentAppelant);
                        detailsListAppelantRepo.save(callerDetail);
                    }
                    break;

                case "imei":
                    if (newDetails.getIdDetail() != null) {
                        DetailImei existingDetail = detailsImeiRepo.find(DetailImei.class, newDetails.getIdDetail());
                        if (existingDetail == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        existingDetail.setHotlistnumber(newDetails.getValue());
                        existingDetail.setDateModif(currentTimestamp);
                        detailsImeiRepo.save(existingDetail);
                    } else {
                        ListImei parentImei = imeiRepo.find(ListImei.class, newDetails.getIdPrincipal());
                        if (parentImei == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        DetailImei imeiDetail = new DetailImei();
                        imeiDetail.setDateModif(currentTimestamp);
                        imeiDetail.setHotlistnumber(newDetails.getValue());
                        imeiDetail.setListImei(parentImei);
                        detailsImeiRepo.save(imeiDetail);
                    }
                    break;

                case "cellid":
                    if (newDetails.getIdDetail() != null) {
                        DetailsListCellid existingDetail = detailsCellidRepo.find(DetailsListCellid.class, newDetails.getIdDetail());
                        if (existingDetail == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        existingDetail.setHotlistnumber(newDetails.getValue());
                        existingDetail.setDateModif(currentTimestamp);
                        detailsCellidRepo.save(existingDetail);
                    } else {
                        ListCellid parentCellid = cellidRepo.find(ListCellid.class, newDetails.getIdPrincipal());
                        if (parentCellid == null) {
                            return ResponseEntity.badRequest().body(newDetails);
                        }
                        DetailsListCellid cellidDetail = new DetailsListCellid();
                        cellidDetail.setDateModif(currentTimestamp);
                        cellidDetail.setHotlistnumber(newDetails.getValue());
                        cellidDetail.setListCellid(parentCellid);
                        detailsCellidRepo.save(cellidDetail);
                    }
                    break;

                default:
                    return ResponseEntity.badRequest().body(newDetails);
            }
            return ResponseEntity.ok(newDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(newDetails);
        }
    }

    @DeleteMapping("/details")
    public ResponseEntity<?> deleteDetails(@RequestBody DetailsFormDTO deleteRequest) {
        if (deleteRequest.getIdDetail() == null) {
            return ResponseEntity.badRequest().body("ID is required for deletion");
        }
        String type = deleteRequest.getType().toLowerCase();
        try {
            switch (type) {
                case "appele":
                    DetailsListAppele appeleDetail = detailsListAppeleRepo.find(DetailsListAppele.class, deleteRequest.getIdDetail());
                    if (appeleDetail != null) {
                        detailsListAppeleRepo.delete(DetailsListAppele.class, appeleDetail.getId());
                        return ResponseEntity.ok().build();
                    }
                    break;

                case "appelant":
                    DetailsListAppelant appelantDetail = detailsListAppelantRepo.find(DetailsListAppelant.class, deleteRequest.getIdDetail());
                    if (appelantDetail != null) {
                        detailsListAppelantRepo.delete(DetailsListAppelant.class,deleteRequest.getIdDetail());
                        return ResponseEntity.ok().build();
                    }
                    break;

                case "imei":
                    DetailImei imeiDetail = detailsImeiRepo.find(DetailImei.class, deleteRequest.getIdDetail());
                    if (imeiDetail != null) {
                        detailsImeiRepo.delete(DetailImei.class,deleteRequest.getIdDetail());
                        return ResponseEntity.ok().build();
                    }
                    break;

                case "cellid":
                    DetailsListCellid cellidDetail = detailsCellidRepo.find(DetailsListCellid.class, deleteRequest.getIdDetail());
                    if (cellidDetail != null) {
                        detailsCellidRepo.delete(DetailsListCellid.class, deleteRequest.getIdDetail());
                        return ResponseEntity.ok().build();
                    }
                    break;

                default:
                    return ResponseEntity.badRequest().body("Invalid type specified");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting record: " + e.getMessage());
        }
    }
}