

const host = window.location.host;
const protocol = window.location.protocol;


$(document).ready(()=>{
console.log(getCookie("user_presence"));
console.log(getCookie("user_jwt"));
    if(!getCookie("user_presence") && !getCookie("user_jwt")){
        console.log("user not logged in");
        $("#login-signup").trigger("click");
    }
});

function getCookie(name) {
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
}

function authoriseWithGoogle(){

            const client_id = "1038305202126-nurp4kb4jdlsp27ep81u77bqpt7astg1.apps.googleusercontent.com";
        const redirect_uri = protocol+"//"+host+"/auth/googleauthcallback";
        const scope = "profile email openid";
      
        let state;
        let uri;
        fetch('/statetoken',{}).then(function (response) {
            return response.json();
        }).then(responseValue => {
            if(responseValue.ok){
                state = responseValue.state;
            }
            console.log(`state in frontend: ${state}`);
            return state;
        }).then(stateVal => {
            uri = "https://accounts.google.com/o/oauth2/v2/auth?redirect_uri="+redirect_uri
                +"&prompt=consent&response_type=code&client_id="+client_id
                +"&scope="+scope+
                "&access_type=offline"+
                "&state="+state+
                "&type="+"signup";

            window.location.href = uri;
        })

};


