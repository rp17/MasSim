package raven.utils;

public class PIDcontroller {
	public float kp = 0.7f;
	public float kd = 0;
	public float ki = 0;
	public float scaler = 5;
	public static final float PI = (float)Math.PI;
	protected float rc = 1/(PI*20);
	
	protected float prevError = 0;
	protected float prevDeriv = 0;
	protected float integrator = 0;
	protected float imax = 20;
	public PIDcontroller(float kp, float kd, float ki) {
		this.kp = kp; this.kd = kd; this.ki = ki;
	}
	
	public void zeroGains(){kp = kd = ki = 0;}
	public float constrain(float val, float max){
		if(val < -max) return -max;
		else if(val > max) return max;
		else return val;
	}
	public void setFreqLPF(float freq){rc = 1/(PI*freq);}
	public void setPrevState(float prevError, float prevDeriv) {this.prevError = prevError; this.prevDeriv = prevDeriv;}
	
	/*
	 * dt is in seconds
	 * 
	 * 
	 */
	public float pidCycle(float error, float dt) {
		float out = 0;
		//float dtSec = dt*1.0e-3f;
		
		float dtSec = dt;
		
		out += kp*error;
		if( dt > 0) {
			if(Math.abs(kd) > 0) {
				float deriv = (error - prevError)/dtSec;
				//deriv = prevDeriv + (dtSec/(rc + dtSec))*(deriv - prevDeriv);
				prevError = error;
				prevDeriv = deriv;
				out += kd*deriv;
			}
			out *= scaler;
			if(Math.abs(ki) > 0) {
				integrator += ki*error*scaler*dtSec;
				integrator = constrain(integrator, imax);
				out += integrator;
			}
		}
		else out *= scaler;
		//System.out.println("Output control = " + out);
		out = constrain(out, 90);
		return out;
	}
	
}
