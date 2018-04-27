package kz.ramanqul.upwork.mark_joachim.SpringExtplorer.controller;

import java.util.Date;

import org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.dto.ErrorDetails;
import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.exception.FileStoreException;

@ControllerAdvice
@RestController
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FileStoreException.class)
    public final @ResponseBody ResponseEntity<ErrorDetails> handleAllExceptions(FileStoreException ex, WebRequest request) {
      ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
      return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(MultipartException.class)
    public @ResponseBody ResponseEntity<ErrorDetails> multipartExceptionHandler(MultipartException e) {
        Throwable th = e.getCause();
        if( th instanceof IllegalStateException ) {
            Throwable cause = th.getCause();
            
            if(cause instanceof  SizeLimitExceededException) {
                SizeLimitExceededException ex = (SizeLimitExceededException) cause;
                ErrorDetails sizeError = new ErrorDetails(new Date(), "FAILED", "Total size of file(s) should not be more than " + (int)(ex.getPermittedSize()/Math.pow(2, 20)) + " MB");
                
                return new ResponseEntity<>(sizeError, HttpStatus.PAYLOAD_TOO_LARGE);
            }
        }
        
        ErrorDetails otherError = new ErrorDetails(new Date(), "FAILED", e.getMessage());
        
        return new ResponseEntity<>(otherError, HttpStatus.PAYLOAD_TOO_LARGE);
    }
    
}
