%%{
  
  machine http_response_common;

#### HTTP PROTOCOL GRAMMAR
# line endings
  CRLF = "\r\n";

# character types
  CTL = (cntrl | 127);
  tspecials = ("(" | ")" | "<" | ">" | "@" | "," | ";" | ":" | "\\" | "\"" | "/" | "[" | "]" | "?" | "=" | "{" | "}" | " " | "\t");

# elements
  token = (ascii -- (CTL | tspecials));

  Reason_Phrase = ( ascii -- ("\r" | "\n") )+ >mark %reason_phrase;

  Status_Code = ( digit+ ) >mark %status_code ;

  http_number = ( digit+ "." digit+ ) ;
  HTTP_Version = ( "HTTP/" http_number ) >mark %http_version ;

  Response_Line = ( HTTP_Version " " Status_Code " " Reason_Phrase CRLF ) ;

  field_name = ( token -- ":" )+ >start_field %write_field;

  field_value = any* >start_value %write_value;

  message_header = field_name ":" " "* field_value :> CRLF;

  Response = Response_Line ( message_header )* ( CRLF @done );

main := Response;

}%%
