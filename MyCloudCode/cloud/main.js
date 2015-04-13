
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function (request, response) {
    var address = request.params.address;
    var Mailgun = require('mailgun');
    var message = request.params.message;
    Mailgun.initialize('sandboxc123454693084d4596e94dc876da3d42.mailgun.org', 'key-b01eb5003e8f53e56a5b7cd4a119de76');

    Mailgun.sendEmail({
      to: address,
      from: "IndistinguishableFromMagic@mit.edu",
      subject: message,
      text: " "
    }, {
      success: function (httpResponse) {
          console.log(httpResponse);
          response.success("Email sent!");
      },
      error: function (httpResponse) {
          console.error(httpResponse);
          response.error("Uh oh, something went wrong");
      }
    });
});