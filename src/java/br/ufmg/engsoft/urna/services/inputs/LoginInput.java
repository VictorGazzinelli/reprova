package br.ufmg.engsoft.urna.services.inputs;

public class LoginInput {
	
	private String body;

	public CreateQuestionInput(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
}
