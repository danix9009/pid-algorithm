/*
* calculate the PID value function
*/
private final void calculatePID() {

    double kProportional = this.getProportionalConstant();
    double kIntegral = this.getIntegralConstant();
    double kDerivative = this.getDerivativeConstant();

    //current time counts
    long now = Clock.ticks();
    double setPt = this.getSetPoint().getValue();
    double controlVar = this.getAverageIn().getValue();
    boolean fault = false;
    if (Double.isNaN(setPt) || Double.isNaN(controlVar)) {
      fault = true;
      }
    
    
    if (this.lastExecuteTime == 0L) {
      this.lastExecuteTime = now;
    }

    long delta = now - this.lastExecuteTime;
    double deltaSecs = (double) delta / 1000.0D;
    double error;
    if (!this.getEnabled()) {
      error = 0.0D;
      syncEffectiveLoad(error);
      if (this.getDirection()) {
        //  this.kPkIconst=this.Kp*this.Ki / 60.0D;
        this.errorSum = -(error / this.kPkIconst);
      } else {
        this.errorSum = error / this.kPkIconst;
      }

      this.lastExecuteTime = 0L;

    } else if(!fault){
      error = this.getSetPoint().getValue() - this.getInPoint().getValue();
      double proportionalGain;
      if(kIntegral != 0.0D){
        proportionalGain = deltaSecs*error;
        this.errorSum += proportionalGain;
        this.kPkIconst = kProportional * kIntegral / 60.0D;
        if(this.getDirection()){
          if(-this.errorSum > maximumOutput /this.kPkIconst){
            this.errorSum = -(maximumOutput / this.kPkIconst);
          }else if(-this.errorSum < minimumOutput / this.kPkIconst){
            this.errorSum = -(minimumOutput / this.kPkIconst);
          }
        }else if(this.errorSum > maximumOutput / this.kPkIconst){
          this.errorSum = maximumOutput / this.kPkIconst;
        }else if(this.errorSum < minimumOutput / this.kPkIconst){
          this.errorSum = minimumOutput / this.kPkIconst;
        }
      }
      
      if (Double.isNaN(this.errorSum) || Double.isInfinite(this.errorSum)) {
        this.errorSum = 0.0D;
        }
      
      proportionalGain = error * kProportional;
      double integralGain = 0.0D;
      integralGain = kProportional * kIntegral * this.errorSum / 60.0D;
      double derivativeGain = kProportional * kDerivative * (error - this.lastError) / deltaSecs;
      this.lastError = error;
      double pv = proportionalGain + integralGain + derivativeGain;
      if(!Double.isNaN(pv)){
        if(this.getDirection()){
          pv = -pv;
        }
        
        if(pv > maximumOutput){
          pv = maximumOutput;
        }else if(pv < minimumOutput){
          pv = minimumOutput;
        }
        
        // synchronize the result value
        sync(pv);
        
        this.lastExecuteTime = now;
      }
    }
  }
  
    private long lastExecuteTime;
    private double kPkIconst;
    private double errorSum=0.0D;
    private double lastError = 0.0D;
    private double maximumOutput = 100.0D;
    private double minimumOutput = 0.0D;
      
