import React, { useState, useEffect, useRef } from 'react';

const SPLASH_SESSION_KEY = 'yasin_telecom_splash_shown';
let globalSplashHasStarted = false;

interface SplashScreenProps {
  onFinish?: () => void;
}

export const SplashScreen: React.FC<SplashScreenProps> = ({ onFinish }) => {
  // Check if splash has already been shown in this session
  const [shouldShow, setShouldShow] = useState(() => {
    if (globalSplashHasStarted) return false;
    try {
      if (sessionStorage.getItem(SPLASH_SESSION_KEY) === 'true') {
        return false;
      }
    } catch {
      // In case sessionStorage is restricted
    }
    globalSplashHasStarted = true;
    return true;
  });

  const [isFadingOut, setIsFadingOut] = useState(false);
  const [stage, setStage] = useState<'BOUNCING' | 'SETTLED' | 'BLOOMING'>('BOUNCING');
  const hasFinishedRef = useRef(false);

  const completeSplash = () => {
    if (hasFinishedRef.current) return;
    hasFinishedRef.current = true;
    try {
      sessionStorage.setItem(SPLASH_SESSION_KEY, 'true');
    } catch {
      // ignore
    }
    setShouldShow(false);
    if (onFinish) onFinish();
  };

  useEffect(() => {
    if (!shouldShow) {
      if (onFinish && !hasFinishedRef.current) {
        hasFinishedRef.current = true;
        onFinish();
      }
      return;
    }

    // Stage 1: Bouncing ball (0.0s - 1.8s)
    // Stage 2: Settled in center (1.8s - 2.1s)
    const settleTimer = window.setTimeout(() => {
      setStage('SETTLED');
    }, 1800);

    // Stage 3: Logo blooms and unfolds outward without shape distortion (2.1s - 2.8s)
    const bloomTimer = window.setTimeout(() => {
      setStage('BLOOMING');
    }, 2100);

    // Fade out begins at 2.85s, total splash completes at 3.0s
    const fadeTimer = window.setTimeout(() => {
      setIsFadingOut(true);
      try {
        sessionStorage.setItem(SPLASH_SESSION_KEY, 'true');
      } catch {
        // ignore
      }
    }, 2850);

    // Complete at exactly 3.15s (smooth fade transition)
    const finishTimer = window.setTimeout(() => {
      completeSplash();
    }, 3150);

    return () => {
      clearTimeout(settleTimer);
      clearTimeout(bloomTimer);
      clearTimeout(fadeTimer);
      clearTimeout(finishTimer);
    };
  }, [shouldShow, onFinish]);

  if (!shouldShow) return null;

  return (
    <div
      className={`fixed inset-0 z-[9999] flex flex-col items-center justify-center overflow-hidden bg-gradient-to-br from-[#020e1a] via-[#061c30] to-[#0a2540] select-none transition-opacity duration-350 ease-out ${
        isFadingOut ? 'opacity-0 pointer-events-none' : 'opacity-100'
      }`}
    >
      {/* Background ambient lighting effects */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-gradient-to-r from-orange-500/15 via-cyan-500/15 to-emerald-500/15 rounded-full blur-3xl pointer-events-none" />

      {/* Radiant Bloom Aura when logo settles in center */}
      <div
        className={`absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[260px] sm:w-[320px] h-[260px] sm:h-[320px] rounded-full bg-gradient-to-tr from-orange-500/25 via-amber-400/20 to-cyan-400/25 blur-2xl transition-all duration-600 pointer-events-none ${
          stage === 'BLOOMING' ? 'scale-125 opacity-100' : 'scale-50 opacity-0'
        }`}
      />

      {/* Stage: Dynamic Ball Bouncing Canvas */}
      <div className="relative w-full h-[60vh] flex items-center justify-center">
        {/* Floor Contact Shadow */}
        <div
          className="absolute bottom-[4vh] w-32 h-6 rounded-[100%] bg-black/60 blur-md pointer-events-none splash-floor-shadow"
          style={{
            animation: 'floorShadowPulse 2.85s ease-in-out forwards',
          }}
        />

        {/* The Logo: Pure 1:1 circular aspect ratio with NO squashing or stretching distortion */}
        <div
          className="relative z-10 w-32 sm:w-36 h-32 sm:h-36 rounded-full cursor-pointer splash-ball-actor"
          style={{
            animation: 'logoBouncingDropSequence 2.85s cubic-bezier(0.25, 1, 0.5, 1) forwards',
          }}
          onClick={() => {
            // Instant skip on click if desired
            setIsFadingOut(true);
            setTimeout(() => {
              completeSplash();
            }, 200);
          }}
        >
          {/* Glowing Aura Ring around the spherical badge */}
          <div
            className={`absolute -inset-1.5 rounded-full transition-all duration-500 ${
              stage === 'BLOOMING'
                ? 'bg-gradient-to-r from-orange-500 via-amber-400 to-cyan-400 opacity-70 blur-md animate-pulse'
                : 'opacity-0'
            }`}
          />

          {/* Logo Badge Image - Strict 1:1 Aspect Ratio without cropping */}
          <div className="relative w-full h-full rounded-full overflow-hidden shadow-2xl ring-3 ring-white/20 bg-white">
            <img
              src="/assets/aistudio/file_000000008d708211963d43c204b0de74_887193df.png"
              alt="ইয়াছিন টেলিকম লোগো"
              className="w-full h-full object-contain block select-none pointer-events-none"
              draggable={false}
            />
          </div>
        </div>
      </div>

      {/* Brand Title and Tagline (Fades in smoothly when logo settles & blooms) */}
      <div
        className={`text-center space-y-1 transition-all duration-500 transform ${
          stage === 'BLOOMING'
            ? 'opacity-100 translate-y-0 scale-100'
            : 'opacity-0 translate-y-3 scale-95'
        }`}
      >
        <h1 className="text-lg sm:text-xl font-black text-transparent bg-clip-text bg-gradient-to-r from-orange-400 via-amber-200 to-cyan-300 tracking-wide drop-shadow-md">
          ইয়াছিন টেলিকম
        </h1>
        <p className="text-xs font-bold text-slate-300 flex items-center justify-center gap-1.5">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping" />
          <span>স্মার্ট শপ ম্যানেজার</span>
        </p>
      </div>

      {/* Elegant 3-Second Loading Progress Line at bottom */}
      <div className="absolute bottom-6 w-32 h-1 bg-white/10 rounded-full overflow-hidden">
        <div
          className="h-full bg-gradient-to-r from-orange-500 via-amber-400 to-emerald-400 rounded-full"
          style={{
            animation: 'splashProgressBar 3s linear forwards',
          }}
        />
      </div>

      {/* Embedded High-Performance Keyframe Animations: ZERO Distortion */}
      <style>{`
        /*
          Natural Vertical Bouncing Trajectory:
          Maintains strict 1:1 aspect ratio (scale: 1, 1) and rotate: 0deg.
          No squashing, no stretching, perfectly round throughout!
        */
        @keyframes logoBouncingDropSequence {
          0% {
            /* Starts high above the viewport */
            transform: translate3d(0, -90vh, 0) scale(1);
            opacity: 0.9;
          }
          22% {
            /* Drop 1: Hits the floor cleanly without squashing */
            transform: translate3d(0, 20vh, 0) scale(1);
          }
          34% {
            /* Apex of first bounce */
            transform: translate3d(0, -7vh, 0) scale(1);
          }
          45% {
            /* Drop 2: Hits the floor again (ড্রপ খাবে) cleanly */
            transform: translate3d(0, 20vh, 0) scale(1);
          }
          58% {
            /* Softly rises and settles in the exact center */
            transform: translate3d(0, 0, 0) scale(1);
          }
          72% {
            /* Settled in center before blooming */
            transform: translate3d(0, 0, 0) scale(1);
          }
          85% {
            /* মেলে গিয়ে: Elegant, uniform bloom outward without distortion */
            transform: translate3d(0, 0, 0) scale(1.08);
            filter: drop-shadow(0 0 20px rgba(249, 115, 22, 0.55)) drop-shadow(0 0 40px rgba(14, 165, 233, 0.4));
          }
          96%, 100% {
            /* Fully bloomed majestic state ready to reveal app */
            transform: translate3d(0, 0, 0) scale(1.06);
            filter: drop-shadow(0 0 25px rgba(249, 115, 22, 0.65)) drop-shadow(0 0 50px rgba(14, 165, 233, 0.45));
          }
        }

        /* Floor Shadow Synchronized with Ball Altitude */
        @keyframes floorShadowPulse {
          0% {
            transform: scale(0.2);
            opacity: 0.1;
          }
          22% {
            /* Ball contacts floor */
            transform: scale(1.1);
            opacity: 0.85;
          }
          34% {
            /* Ball bounces high */
            transform: scale(0.45);
            opacity: 0.25;
          }
          45% {
            /* Ball second floor hit */
            transform: scale(1.0);
            opacity: 0.75;
          }
          58% {
            /* Ball moves to center */
            transform: scale(0.6);
            opacity: 0.3;
          }
          75%, 100% {
            transform: scale(0.7);
            opacity: 0.2;
          }
        }

        @keyframes splashProgressBar {
          0% {
            width: 0%;
          }
          100% {
            width: 100%;
          }
        }
      `}</style>
    </div>
  );
};
