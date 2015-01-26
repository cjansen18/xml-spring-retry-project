package com.chrisjansen.xmlspringretry;


import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

/**
 * @author: Christopher M. Jansen
 * @since: 1/26/15 9:53 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("retry-context.xml")
public class RetryTest {

    private static Logger LOG = LoggerFactory.getLogger(RetryTest.class);

    @Autowired
    private RetryTemplate retryTemplate;

    /**
     * Simple test to show retry logic
     */
    @Test
    public void invokeWithRetrySupport() {
        LOG.info("Running WITH RETRY");
        Boolean returnValue=Boolean.FALSE;
        try {
             returnValue=retryTemplate.execute(new RetryCallback<Boolean>() {
                public Boolean doWithRetry( RetryContext retryContext ) throws Exception {
                    Random r = new Random();
                    int randomNumber = r.nextInt(5);
                    LOG.info("Try number: "+retryContext.getRetryCount()+" with generated random number: "+randomNumber);
                    if(randomNumber==4){
                        LOG.debug("We have a four in the try number:"+retryContext.getRetryCount());
                        return Boolean.TRUE;
                    }
                    else{
                        //This exception is configured in retry-context.xml to trigger the retry logic!
                        throw new org.springframework.jms.UncategorizedJmsException("No fours for you :(");
                    }
                }
            });

            Assert.assertTrue(returnValue);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * This method should not use the retry logic as the exception is not configured for inclusion
     */
    @Test(expected = IOException.class)
    public void invokeWithoutRetrySupport() {
        LOG.info("Running WITH RETRY");
        try {
            retryTemplate.execute(new RetryCallback<Boolean>() {
                public Boolean doWithRetry( RetryContext retryContext ) throws Exception {
                    Random r = new Random();
                    int randomNumber = r.nextInt(10);
                    LOG.info("Try number: "+retryContext.getRetryCount()+" with generated random number: "+randomNumber);
                    if(randomNumber==4){
                        //return Double.valueOf("We have a four in the try number: "+retryContext.getRetryCount());
                        LOG.debug("We have a four in the try number:"+retryContext.getRetryCount());
                        return Boolean.TRUE;
                    }
                    else{
                        //This exception is configured to trigger the retry logic!
                        throw new IOException("No fours for you :(");
                    }
                }
            });
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

