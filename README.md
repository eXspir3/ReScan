# ReScan 1.1.0

*ReScan is a tool to Help you resend certain HTTP / HTTPS Requests and specify assertions for the incoming HTTP / HTTPS response.*

## CommandLine Arguments:

  **-f** Specify the .txt File containing your Requests and Options in special formatting (guide below)  
  **-m** Specify the Mode to be used, currently supported:  
  **-m 0** Send the Requests and Save to Responses with no further Checking  
  **-m 1** Send the Requests and Check the Responses with your Assertions and save only the Errors  
    
  Encryption, Decryption:  
  **-encryptResults** Encrypts the Results via AES-128 GCM and saves the AES Key in a File encrypted  
                     &nbsp;    with the specified Public Key  
  **-pubKey** Specify a RSA-PublicKey (.pem) for Encryption - this is mandatory when using -encryptResults  
    
  **-decryptRequests** You can load an AES-128 GCM NoPadding encrypted Requests File by specifying the aesKey  
                    &nbsp;      stored in a File using RSA/OAEP/SHA512withMGF1 Encryption and the RSA privateKey to decrypt this File  
  **-privKey** Specify an RSA-PrivateKey-File (.pem PKCS-8) for Decryption - this is mandatory when using -decryptRequests  
  **-aesKey** Specify an AES-Key-File for Decryption - this is mandatory when using -decryptRequests  
    
    Check out the used Encryption Algorithms: https://github.com/eXspir3/RSAoverAES
    
## Using Encryption:
  
  In order to make use of the Encryption Functionality, encrypt your Requests File / decrypt your Results File with:
  **RSAoverAES** https://github.com/eXspir3/RSAoverAES
                    
          
## -f *.txt Formatting:

  1. Fill in your RAW HTTP / HTTPS Request to be resent  
  2. add the Line --assertions and specify the Assertion Methods for the Request below --assertions  
    **--assertions** is mandatory even if **no options** are specified  
  3. add the Line --nextRequest to specify further requests below --nextRequest  
  
    Example *.txt File:

       GET http://google.at/ HTTP/1.1  
       User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0  
       Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8  
       Accept-Language: de,en-US;q=0.7,en;q=0.3   
       Connection: keep-alive  
       Upgrade-Insecure-Requests: 1  
       Proxy-Connection: Keep-Alive  
       Host: google.at     
       --assertions     
       AssertHeader:Content-Type:text/html; charset=utf-8     
       AssertStatusCode:205     
       BodyContains:Moved     
       --nextRequest
       GET http://google.at/ HTTP/1.1
       User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0
       Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
       Accept-Language: de,en-US;q=0.7,en;q=0.3
       Connection: keep-alive
       Upgrade-Insecure-Requests: 1
       Proxy-Connection: Keep-Alive
       Host: google.at
       --assertions
       AssertStatusCode:301 
  
          
## Supported Assertion Methods:

  **AssertHeader:** Assert that the Header of the response is something specific   
      Format: AssertHeader:Header:Value  
        e.g.: AssertHeader:Content-Type:text/html  

  **AssertStatusCode:** Assert that the Response contains a specific StatusCode  
      Format: AssertStatusCode:numericStatusCode  
        e.g.: AssertStatusCode:404  

  **BodyContains:** Specify a REGEX that should match to the body of the response or parts of it  
      Format: BodyContains:regex  
        e.g.: BodyContains:(unauthorized)  
            
          
