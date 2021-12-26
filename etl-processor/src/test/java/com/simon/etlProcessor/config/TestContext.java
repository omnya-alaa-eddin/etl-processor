package com.simon.etlProcessor.config;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class TestContext implements Context{
  public TestContext() {}
  public String getAwsRequestId(){
    return new String("495b12a8-xmpl-4eca-8168-160484189f99");
  }
  public String getLogGroupName(){
    return new String("/aws/lambda/my-function");
  }
  public LambdaLogger getLogger(){
    return new TestLogger();
  }
@Override
public String getLogStreamName() {
    // TODO Auto-generated method stub
    return null;
}
@Override
public String getFunctionName() {
    // TODO Auto-generated method stub
    return null;
}
@Override
public String getFunctionVersion() {
    // TODO Auto-generated method stub
    return null;
}
@Override
public String getInvokedFunctionArn() {
    // TODO Auto-generated method stub
    return null;
}
@Override
public CognitoIdentity getIdentity() {
    // TODO Auto-generated method stub
    return null;
}
@Override
public ClientContext getClientContext() {
    // TODO Auto-generated method stub
    return null;
}
@Override
public int getRemainingTimeInMillis() {
    // TODO Auto-generated method stub
    return 0;
}
@Override
public int getMemoryLimitInMB() {
    // TODO Auto-generated method stub
    return 0;
}

}