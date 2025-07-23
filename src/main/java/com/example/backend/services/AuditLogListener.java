package com.example.backend.services;
import com.example.backend.dao.AuditLogRepository;
import com.example.backend.entities.AuditLog;
import com.example.backend.entities.User;
import com.example.backend.entities.dto.AuditResponse;
import com.example.backend.entities.dto.UserDto;
import com.example.backend.utils.AuditStatementInspector;
import com.example.backend.utils.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuditLogListener {

    private static AuditLogRepository staticAuditLogRepository;
    private static UserService staticUserService;

    @Autowired
    public void setAuditLogRepository(AuditLogRepository auditLogRepository,UserService userService) {
        staticAuditLogRepository = auditLogRepository;
        staticUserService = userService;
    }

    @PostPersist
    public void logInsert(Object entity) {
        saveAuditLog(entity, "INSERT");
    }

    @PostUpdate
    public void logUpdate(Object entity) {
        saveAuditLog(entity, "UPDATE");
    }

    @PostRemove
    public void logDelete(Object entity) {
        saveAuditLog(entity, "DELETE");
    }

    private void saveAuditLog(Object entity, String action) {
        String tableName = getTableName(entity);
        Object affectedRowId = getEntityId(entity);
        Long userId = null;
        if (RequestContext.getCurrentUserId()!=null) {
            userId = RequestContext.getCurrentUserId();
        }
        String sql = AuditStatementInspector.getCurrentSql();
        AuditLog log = new AuditLog(tableName, affectedRowId.toString(), userId, action, LocalDateTime.now(),sql);
        staticAuditLogRepository.save(log);
    }

    private String getTableName(Object entity) {
        Table tableAnnotation = entity.getClass().getAnnotation(Table.class);
        if (tableAnnotation != null) {
            String schema = tableAnnotation.schema();
            String tableName = tableAnnotation.name();
            if (!schema.isEmpty()) {
                return schema + "." + tableName;
            }
            return tableName;
        }
        return entity.getClass().getSimpleName();
    }

    private Object getEntityId(Object entity) {
        try {
            Field field;
            try {
                field = entity.getClass().getDeclaredField("id");
            } catch (NoSuchFieldException e) {
                field = findPrimaryKeyField(entity);
                if (field == null) {
                    System.err.println("No primary key field found for " + entity.getClass().getSimpleName());
                    return null;
                }
            }
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            System.err.println("Could not access primary key field in " + entity.getClass().getSimpleName());
            return null;
        }
    }

    private Field findPrimaryKeyField(Object entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public List<AuditResponse> getAuditList(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = staticAuditLogRepository.findAllByModifiedAtBetween(startDate, endDate);
        List<AuditResponse> responses = new ArrayList<>();
        for (AuditLog log : logs) {
            UserDto user = new UserDto();
            AuditResponse response = new AuditResponse();
            if(log.getUserId()!=null){
                user = staticUserService.findUserById(log.getUserId());
                response.setUsername(user.getUsername());
                response.setUserEmail(user.getEmail());
            }
            response.setTableName(log.getTableName());
            response.setAffectedRowId(log.getAffectedRowId());
            response.setAction(log.getAction());
            response.setModifiedAt(log.getModifiedAt());
            response.setSqlStatement(log.getSqlQuery());
            responses.add(response);
        }
        return responses;
    }
}
