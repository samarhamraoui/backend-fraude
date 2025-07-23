package com.example.backend.services;

import com.example.backend.dao.BloacageLogRepository;
import com.example.backend.dao.DecisionFraudeRepository;
import com.example.backend.dao.ReglesFraudeRepository;
import com.example.backend.entities.BlocageLog;
import com.example.backend.entities.DecisionFraude;
import com.example.backend.entities.ReglesFraude;
import com.example.backend.entities.dto.*;
import com.example.backend.utils.OperatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class BlocageServiceImpl implements BlockingService{
    private final DecisionFraudeRepository decisionRepository;
    private final OperatorUtil operatorUtil;
    private final BloacageLogRepository bloacageLogRepository;
    private final ReglesFraudeRepository reglesFraudeRepository;

    @Autowired
    public BlocageServiceImpl(DecisionFraudeRepository decisionRepository,OperatorUtil operatorUtil,BloacageLogRepository bloacageLogRepository,ReglesFraudeRepository reglesFraudeRepository) {
        this.decisionRepository = decisionRepository;
        this.operatorUtil = operatorUtil;
        this.bloacageLogRepository = bloacageLogRepository;
        this.reglesFraudeRepository= reglesFraudeRepository;
    }


    @Override
    public BlockingResponseDTO processBlockingRequest(BlockingRequestDTO requestDto, String username) {
        String phoneNumber = requestDto.getPhoneNumber();
        BlockingType blockingType = requestDto.getBlockingType();
        NetworkType networkType = requestDto.getNetworkType();

        NumberBlockingResult result = executeBlockingCommand(phoneNumber, blockingType, networkType, username, 0);

        BlockingResponseDTO response = BlockingResponseDTO.builder()
                .message(result.getMessage())
                .success(!"error".equals(result.getStatus()))
                .results(Arrays.asList(result))
                .build();

        return response;
    }

    @Override
    public BlockingResponseDTO processBatchBlockingRequest(BlockingRequestDTO requestDto, String username) {
        List<String> phoneNumbers = requestDto.getPhoneNumbers();
        BlockingType blockingType = requestDto.getBlockingType();
        NetworkType networkType = requestDto.getNetworkType();

        List<NumberBlockingResult> results = new ArrayList<>();
        boolean hasErrors = false;

        for (int i = 0; i < phoneNumbers.size(); i++) {
            String phoneNumber = phoneNumbers.get(i).trim();
            NumberBlockingResult result = executeBlockingCommand(phoneNumber, blockingType, networkType, username, i);
            results.add(result);

            if ("error".equals(result.getStatus())) {
                hasErrors = true;
            }
        }

        String message = hasErrors ? "Some operations completed with errors" : "All operations completed successfully";

        return BlockingResponseDTO.builder()
                .message(message)
                .success(!hasErrors)
                .results(results)
                .build();
    }


    private NumberBlockingResult executeBlockingCommand(String phoneNumber, BlockingType blockingType, NetworkType networkType,
                                                        String username, int index) {
        String operatorName = operatorUtil.getCurrentOperator();
        String countryPrefix = operatorUtil.getCountryPrefix(operatorName);
        String cmdPrefix = operatorUtil.getCommandPrefix(operatorName);

        NumberBlockingResult result = NumberBlockingResult.builder()
                .index(index)
                .phoneNumber(phoneNumber)
                .status("pending")
                .message("Processing")
                .build();

        // Always log the action at the beginning, regardless of outcome
        try {
            logAction(blockingType, phoneNumber, username, "ATTEMPT");
        } catch (Exception e) {
            System.err.println("Error logging attempt: " + e.getMessage());
        }

        try {
            String typeB = null;
            Process proc = null;
            String command = buildCommand(blockingType, networkType, cmdPrefix, phoneNumber);

            // Execute WhiteList operation differently as it doesn't require a shell command
            if (blockingType == BlockingType.WHITE_LIST) {
                addToWhiteList(phoneNumber, countryPrefix, username);
                result.setStatus("success");
                result.setMessage("Number has been added to whiteList");

                // Log the successful outcome
                try {
                    logAction(blockingType, phoneNumber, username, "SUCCESS");
                } catch (Exception e) {
                    System.err.println("Error logging whitelist success: " + e.getMessage());
                }
                return result;
            }

            // Execute shell command
            if (command != null) {
                System.out.println("About to execute: " + command);
                proc = Runtime.getRuntime().exec(new String[] { "sh", "-c", command });
                System.out.println("Command executed, waiting...");
                proc.waitFor(60, TimeUnit.SECONDS);
                System.out.println("Process finished: Exit code: " + proc.exitValue());
                // Process command output
                String outputCode = processCommandOutput(proc, blockingType, phoneNumber, countryPrefix, username);
                result = processResultCode(outputCode, blockingType, phoneNumber, result);

                // Log the outcome
                try {
                    String outcome = "error".equals(result.getStatus()) ? "ERROR" : "SUCCESS";
                    logAction(blockingType, phoneNumber, username, outcome + " - " + result.getMessage());
                } catch (Exception e) {
                    System.err.println("Error logging command result: " + e.getMessage());
                }
            } else {
                result.setStatus("error");
                result.setMessage("Invalid blocking type or network type");

                // Log the error
                try {
                    logAction(blockingType, phoneNumber, username, "ERROR - Invalid blocking type or network type");
                } catch (Exception e) {
                    System.err.println("Error logging invalid type error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            result.setStatus("error");
            result.setMessage("Error executing blocking command: " + e.getMessage());

            // Log the exception
            try {
                logAction(blockingType, phoneNumber, username, "ERROR - " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Error logging command execution error: " + ex.getMessage());
            }
        }

        return result;
    }

    private String buildCommand(BlockingType blockingType, NetworkType networkType, String cmdPrefix, String phoneNumber) {
        String command = null;

//        if (blockingType == BlockingType.BLOCAGE && networkType == NetworkType.OFFNET) {
//            command = cmdPrefix + " java -jar /mediation/bin/BLKOFFNET.jar " + phoneNumber;
//        } else if (blockingType == BlockingType.DEBLOCAGE && networkType == NetworkType.OFFNET) {
//            command = cmdPrefix + " java -jar /mediation/bin/UNBLKOFFNET.jar " + phoneNumber;
//        } else if (blockingType == BlockingType.BLOCAGE && networkType == NetworkType.ONNET) {
//            command = cmdPrefix + " java -jar /mediation/bin/BLKONNET.jar " + phoneNumber;
//        } else if (blockingType == BlockingType.DEBLOCAGE && networkType == NetworkType.ONNET) {
//            command = cmdPrefix + " java -jar /mediation/bin/dblkwhite.jar " + phoneNumber;
//        } else if (blockingType == BlockingType.BLOCAGE_SMS) {
//            command = cmdPrefix + " /mediation/bin/dblksms " + phoneNumber;
//        } else if (blockingType == BlockingType.DEBLOCAGE_SMS) {
//            command = cmdPrefix + " /mediation/bin/dblksms " + phoneNumber;
//        }

        Optional<ReglesFraude> rgBlock = reglesFraudeRepository.findById(60); //For tt only
        Optional<ReglesFraude> rgDeblock = reglesFraudeRepository.findById(1000);
        if (blockingType == BlockingType.BLOCAGE) {
            command = cmdPrefix + " java -jar /mediation/bin/blkmsisdn.jar " + phoneNumber + ' ' + rgBlock.get().getId();
        } else if (blockingType == BlockingType.DEBLOCAGE) {
            command = cmdPrefix + " java -jar /mediation/bin/dblkwhite.jar " + phoneNumber + ' ' + rgDeblock.get().getId();
        }
        System.out.println(command);
     return command;
    }

    private void addToWhiteList(String phoneNumber, String countryPrefix, String username) {
        try {
            DecisionFraude df = new DecisionFraude();
            Calendar calendar = Calendar.getInstance();
            Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

            Optional<ReglesFraude> rg = reglesFraudeRepository.findById(60); // 60 for TT only

            df.setDateModif(currentTimestamp);
            df.setNomUtilisateur(username);
            //df.setMsisdn(countryPrefix + phoneNumber);
            df.setMsisdn(phoneNumber);
            df.setDateDecision(currentTimestamp);
            df.setFlag(0);
            df.setRegle(rg.get());
            df.setDecision("W");
            decisionRepository.save(df);
        } catch (Exception e) {
            // Log error but continue execution
            System.err.println("Error saving to whitelist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enhanced logging method that logs all actions with their status
     */
    private void logAction(BlockingType blockingType, String phoneNumber, String username, String status) {
        try {
            BlocageLog log = new BlocageLog();
            log.setAction(blockingType + " num : " + phoneNumber + " - " + status);
            log.setUsername(username);
            log.setDateAction(new Timestamp(System.currentTimeMillis()));
            bloacageLogRepository.save(log);
        } catch (Exception e) {
            // Log error but don't throw it to prevent interrupting the main flow
            System.err.println("Error saving blocage log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Legacy logging method for backward compatibility
     */
    private void logAction(BlockingType blockingType, String phoneNumber, String username) {
        logAction(blockingType, phoneNumber, username, "EXECUTED");
    }

    private String processCommandOutput(Process proc, BlockingType blockingType, String phoneNumber,
                                        String countryPrefix, String username) throws Exception {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        StringBuilder outputLog = new StringBuilder();
        String s = null;
        String outputCode = "0";

        // Read the output from the command
        while ((s = stdInput.readLine()) != null) {
            outputLog.append("STDOUT: ").append(s).append("\n");
            if (s.contains("1") || s.contains("2")) {
                outputCode = s;

                try {
                    // Update decision in database
                    DecisionFraude df = new DecisionFraude();
                    Calendar calendar = Calendar.getInstance();
                    Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

                    df.setDateModif(currentTimestamp);
                    df.setNomUtilisateur(username);
                    //df.setMsisdn(countryPrefix + phoneNumber);
                    df.setMsisdn(phoneNumber);

                    if (blockingType == BlockingType.BLOCAGE || blockingType == BlockingType.BLOCAGE_SMS) {
                        df.setDecision("D");
                    } else {
                        df.setDecision("W");
                    }

                    df.setDateDecision(currentTimestamp);
                    df.setFlag(0);
                    //df.setRegle(rg.get());

                    decisionRepository.save(df);
                } catch (Exception e) {
                    // Log error but continue with the operation
                    System.err.println("Error saving decision: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Check for errors
        StringBuilder errorOutput = new StringBuilder();
        while ((s = stdError.readLine()) != null) {
            outputLog.append("STDERR: ").append(s).append("\n");
            errorOutput.append(s);
        }

        if (errorOutput.length() > 0) {
            System.err.println(errorOutput.toString());
            //log.error("Error in command execution: {}", errorOutput.toString());

            // Log the specific error message from the command
            try {
                logAction(blockingType, phoneNumber, username, "COMMAND ERROR - " + errorOutput.toString());
            } catch (Exception e) {
                // Ensure this doesn't throw even if logging fails
                System.err.println("Failed to log command error: " + e.getMessage());
            }
            return "error";
        }
        System.out.println("Execution log:\n" + outputLog);
        return outputCode;
    }

    private NumberBlockingResult processResultCode(String outputCode, BlockingType blockingType,
                                                   String phoneNumber, NumberBlockingResult result) {
        if (outputCode.equals("error")) {
            result.setStatus("error");
            result.setMessage("Error while processing the request");
            return result;
        }

        if (blockingType == BlockingType.BLOCAGE || blockingType == BlockingType.BLOCAGE_SMS) {
            if (outputCode.contains("1")) {
                result.setStatus("success");
                result.setMessage("Number has been blocked");
            } else if (outputCode.contains("2")) {
                result.setStatus("warning");
                result.setMessage("Number is already blocked");
            } else {
                result.setStatus("error");
                result.setMessage("Error while blocking number");
            }
        } else if (blockingType == BlockingType.DEBLOCAGE || blockingType == BlockingType.DEBLOCAGE_SMS) {
            if (outputCode.contains("1")) {
                result.setStatus("success");
                result.setMessage("Number has been unblocked");
            } else if (outputCode.contains("2")) {
                result.setStatus("warning");
                result.setMessage("Number is already unblocked");
            } else {
                result.setStatus("error");
                result.setMessage("Error while unblocking number");
            }
        }

        return result;
    }
}