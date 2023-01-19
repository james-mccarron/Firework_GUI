package Assignment5_17jpm5;
import java.util.ArrayList;

/**
 * This class manages the simulation.  It draws the Roman candle and launches 8 stars of various colours.
 * The class also manages all the other particle effects: the sparks emitted by the star, the 
 * launch sparks and the delay charge sparks.
 * @author Alan McLeod
 * @version 1.1
 */
public class ParticleManager {

	private double deltaTime;		// seconds
	// Stores the time of the last star launch
	private double lastTime;		// seconds
	private Environment env;
	private LaunchTube tube;
	private StarSparkEmitter starSparkEmit;
	private LaunchSparkEmitter launchSparkEmit;
	private DelaySparkEmitter delaySparkEmit;
	// This ArrayList will hold all the generated particles.
	private ArrayList<Particle> fireworks = new ArrayList<>();
	private int numStars = 8;
	private int countStars = 0;
	private double starLaunchTime;
	private double maxExitVelocity;
	
	/**
	 * The ParticleManager constructor
	 * @param windVelocity The wind velocity in m/sec.
	 * @param launchAngle The launch angle of the Roman candle in degrees off the vertical.
	 * @throws EnvironmentException If the wind velocity is not between -20 and 20 m/sec.
	 * @throws EmitterException If the launch angle is not between -15 and 15 degrees.
	 */
	public ParticleManager(double windVelocity, double launchAngle) throws EnvironmentException, EmitterException {
		env = new Environment(windVelocity);
		double la = Math.PI * launchAngle / 180.0;	// radians
		// Position the star emitter at the end of the roman candle.  Use a launch velocity of 22 m/sec
		// and add a 2 degree random variation to the launch angle.
		maxExitVelocity = 22;
		tube = new LaunchTube(Math.sin(la), Math.cos(la), 0, 0, maxExitVelocity, launchAngle, 2);
		lastTime = 0;
	}
	
	/**
	 * Launches a single star at the supplied absolute time and adds one set of launch sparks.
	 * @param time The absolute time in seconds.  The first star will be launched at time=0.
	 */
	public void start(double time) {
		// Add some variation to the star's exit velocity
		tube.setExitVelocity(maxExitVelocity - 2 * Math.random());
		// Launch the star
		ArrayList<Star> starSet = tube.launch(time);
		if (starSet.size() == 0)
			return;
		Star singleStar = starSet.get(0);
		starLaunchTime = time;
		// Add the star to the particles collection.
		fireworks.add(singleStar);
		// Create the spark emitters using the initial position and velocity of the star.
		double[] position = singleStar.getPosition();
		double[] velocity = singleStar.getVelocity();
		double launchAngle = tube.getLaunchAngle();
		try {
			// Star sparks of the same colour as the star will be launched at 3 m/sec in all directions.
			starSparkEmit = new StarSparkEmitter(position[0], position[1], velocity[0], velocity[1], 3, 0, 180);
			starSparkEmit.setColour(singleStar.getColour());
			// Launch sparks will be launched at 20 m/sec within 3 degrees of the star's launch angle.
			launchSparkEmit = new LaunchSparkEmitter(position[0], position[1], 0, 0, 20, launchAngle, 3);
			// Delay charge sparks will be sprayed out at 2.2 m/sec.
			delaySparkEmit = new DelaySparkEmitter(position[0], position[1], 0, 0, 2.2, launchAngle, 90);
		} catch (EmitterException e) {
			// Not likely to get here unless the angles are not legal.
			System.out.println(e.getMessage());
			return;
		}
		// Add launch sparks to "push" the star out.
		fireworks.addAll(launchSparkEmit.launch(time));		
	} // end start method
	
	/**
	 * This method updates the simulation.
	 * @param time The absolute time in seconds. The simulation was started at time = 0;
	 */
	private void update(double time) {
		deltaTime = time - lastTime;
		lastTime = time;
		int index = 0;
		Particle firework;
		// Clean out dead fireworks
		do {
			firework = fireworks.get(index);
			if (time - firework.getCreationTime() >= firework.getLifetime()) {
				// Get rid of the star spark emitter if the star is gone.
				if (firework instanceof Star)
					starSparkEmit = null;
				fireworks.remove(index);
			} else
				index++;
		} while (fireworks.size() > 0 && index < fireworks.size());
		// Update positions
		for (Particle fire : fireworks) {
			fire.updatePosition(time, deltaTime, env);
			// Move the star spark emitter along with the star.
			if (fire instanceof Star)
				starSparkEmit.setPosition(fire.getPosition());
		}
		// Keep adding delay charge sparks until 3.5 seconds are up.
		if (time - starLaunchTime < 3.5) {
			fireworks.addAll(delaySparkEmit.launch(time));
		}
		// Add star sparks as long as the starSpar emitter exists
		if (starSparkEmit != null)
			fireworks.addAll(starSparkEmit.launch(time));
		// If all the particles associated with the previous star are all gone, then prevent the particle 
		// collection from becoming empty by adding delay charge sparks, and then start the launch
		// of another star.
		if (fireworks.size() == 0) {
			if (countStars < numStars - 1) {
				fireworks.addAll(delaySparkEmit.launch(time));
				countStars++;
				// Launch another star
				start(time);
			} else {
				// Stop the simulation after 8 stars have been launched.  The collection will be empty.
				return;
			}
		}		
	} // end update

	/**
	 * Sets the wind velocity from the environment, in metres per second.
	 * @param wind Wind velocity in metres per second.
	 */
	public void setWindVelocity(double wind) {
		env.setWindVelocity(wind);
	} // end setWindVelocity

	/**
	 * Sets the launch angle of the tube and the launch spark emitter in degrees.
	 * @param firingAngle Firing angle at launch in degrees.
	 */
	public void setLaunchAngle(double firingAngle) {
		tube.setLaunchAngle(firingAngle);
		launchSparkEmit.setLaunchAngle(firingAngle);
	} // end setLaunchAngle
	
	/**
	 * Allows the position of the launch tube end to be moved during the simulation.
	 * @param position The tip of the launch tube's (x, y) positions in metres.
	 */
	public void setTipPosition(double[] position) {
		tube.setPosition(position);
		launchSparkEmit.setPosition(position);
		delaySparkEmit.setPosition(position);
	} // end setTipPosition
	
	/**
	 * An accessor for the collection of particles.
	 * For this to work properly, each particle type should have have its own clone method.  (But they
	 * do not, which is OK...)
	 * @param time The absolute time in seconds. The simulation started at time = 0.
	 * @return The collection of particles in an ArrayList.
	 */
	public ArrayList<Particle> getFireworks(double time) {
		update(time);
		ArrayList<Particle> copy = new ArrayList<>(fireworks.size());
		for (Particle firework : fireworks)
			copy.add(firework);
		return copy;
	}
	
} // end ParticleManager class

