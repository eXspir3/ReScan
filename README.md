# ReScan 1.0.6

*ReScan is a tool to Help you resend certain HTTP / HTTPS Requests and specify assertions for the incoming HTTP / HTTPS response.*

## CommandLine Arguments:

  **-f** Specify the .txt File containing your Requests and Options in special formatting (guide below)  
  **-m** Specify the Mode to be used, currently supported:  
  **-m 0** Send the Requests and Save to Responses with no further Checking  
  **-m 1** Send the Requests and Check the Responses with your Assertions and save only the Errors  
          
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
       --options
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
            
          
