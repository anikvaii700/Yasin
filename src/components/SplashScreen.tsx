import React, { useState, useEffect } from 'react';

interface SplashScreenProps {
  onFinish?: () => void;
}

export const SplashScreen: React.FC<SplashScreenProps> = ({ onFinish }) => {
  const [isVisible, setIsVisible] = useState(true);
  const [isFadingOut, setIsFadingOut] = useState(false);
  const [stage, setStage] = useState<'BOUNCING' | 'SETTLED' | 'BLOOMING'>('BOUNCING');

  useEffect(() => {
    // Stage 1: Bouncing ball (0.0s - 1.8s)
    // Stage 2: Settled in center (1.8s - 2.1s)
    const settleTimer = window.setTimeout(() => {
      setStage('SETTLED');
    }, 1800);

    // Stage 3: Logo blooms and unfolds outward (2.1s - 2.8s)
    const bloomTimer = window.setTimeout(() => {
      setStage('BLOOMING');
    }, 2100);

    // Fade out begins at 2.85s, total splash completes at 3.0s
    const fadeTimer = window.setTimeout(() => {
      setIsFadingOut(true);
    }, 2850);

    // Complete at exactly 3.2s (smooth fade transition)
    const finishTimer = window.setTimeout(() => {
      setIsVisible(false);
      if (onFinish) onFinish();
    }, 3200);

    return () => {
      clearTimeout(settleTimer);
      clearTimeout(bloomTimer);
      clearTimeout(fadeTimer);
      clearTimeout(finishTimer);
    };
  }, [onFinish]);

  if (!isVisible) return null;

  return (
    <div
      className={`fixed inset-0 z-[9999] flex flex-col items-center justify-center overflow-hidden bg-gradient-to-br from-[#020e1a] via-[#061c30] to-[#0a2540] select-none transition-opacity duration-400 ease-out ${
        isFadingOut ? 'opacity-0 pointer-events-none' : 'opacity-100'
      }`}
    >
      {/* Background ambient lighting effects */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-r from-orange-500/15 via-cyan-500/15 to-emerald-500/15 rounded-full blur-3xl pointer-events-none" />

      {/* Radiant Bloom Aura when logo unfolds */}
      <div
        className={`absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[340px] sm:w-[420px] h-[340px] sm:h-[420px] rounded-full bg-gradient-to-tr from-orange-500/30 via-amber-400/25 to-cyan-400/30 blur-2xl transition-all duration-700 pointer-events-none ${
          stage === 'BLOOMING' ? 'scale-125 opacity-100' : 'scale-50 opacity-0'
        }`}
      />

      {/* Stage: Dynamic Ball Bouncing Canvas */}
      <div className="relative w-full h-[65vh] flex items-center justify-center">
        {/* Floor Contact Shadow (Reacts with bouncing ball) */}
        <div
          className="absolute bottom-[4vh] w-40 h-8 rounded-[100%] bg-black/60 blur-md pointer-events-none splash-floor-shadow"
          style={{
            animation: 'floorShadowPulse 2.8s ease-in-out forwards',
          }}
        />

        {/* Shockwave Rings on Floor Impact */}
        <div
          className="absolute bottom-[6vh] w-24 h-6 rounded-[100%] border-2 border-orange-400/60 pointer-events-none splash-shockwave-1"
          style={{
            animation: 'shockwave1 2.8s ease-out forwards',
          }}
        />
        <div
          className="absolute bottom-[6vh] w-24 h-6 rounded-[100%] border border-cyan-400/50 pointer-events-none splash-shockwave-2"
          style={{
            animation: 'shockwave2 2.8s ease-out forwards',
          }}
        />

        {/* The Logo (Drops from top to bottom like a ball, bounces, settles in center, and blooms) */}
        <div
          className="relative z-10 w-44 sm:w-56 h-44 sm:h-56 rounded-full cursor-pointer splash-ball-actor"
          style={{
            animation: 'logoBouncingDropSequence 2.85s cubic-bezier(0.25, 1, 0.5, 1) forwards',
          }}
          onClick={() => {
            // Instant skip on click if desired
            setIsFadingOut(true);
            setTimeout(() => {
              setIsVisible(false);
              if (onFinish) onFinish();
            }, 250);
          }}
        >
          {/* Glowing Aura Ring around the spherical badge */}
          <div
            className={`absolute -inset-2 rounded-full transition-all duration-700 ${
              stage === 'BLOOMING'
                ? 'bg-gradient-to-r from-orange-500 via-amber-400 to-cyan-400 opacity-80 blur-md animate-pulse'
                : 'opacity-0'
            }`}
          />

          {/* Logo Badge Image */}
          <div className="relative w-full h-full rounded-full overflow-hidden shadow-2xl ring-4 ring-white/20 bg-white">
            <img
              src="/assets/aistudio/file_000000008d708211963d43c204b0de74_887193df.png"
              alt="ইয়াছিন টেলিকম লোগো"
              className="w-full h-full object-cover block select-none pointer-events-none"
              draggable={false}
            />
          </div>
        </div>
      </div>

      {/* Brand Title and Tagline (Fades in smoothly when logo settles & blooms) */}
      <div
        className={`text-center space-y-1.5 transition-all duration-600 transform ${
          stage === 'BLOOMING'
            ? 'opacity-100 translate-y-0 scale-100'
            : 'opacity-0 translate-y-4 scale-95'
        }`}
      >
        <h1 className="text-xl sm:text-2xl font-black text-transparent bg-clip-text bg-gradient-to-r from-orange-400 via-amber-200 to-cyan-300 tracking-wide drop-shadow-md">
          ইয়াছিন টেলিকম
        </h1>
        <p className="text-xs sm:text-sm font-bold text-slate-300 flex items-center justify-center gap-1.5">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping" />
          <span>স্মার্ট শপ ম্যানেজার</span>
        </p>
      </div>

      {/* Elegant 3-Second Loading Progress Line at bottom */}
      <div className="absolute bottom-6 w-36 h-1 bg-white/10 rounded-full overflow-hidden">
        <div
          className="h-full bg-gradient-to-r from-orange-500 via-amber-400 to-emerald-400 rounded-full"
          style={{
            animation: 'splashProgressBar 3s linear forwards',
          }}
        />
      </div>

      {/* Embedded High-Performance Keyframe Animations */}
      <style>{`
        /*
          Sequence Timing (Total ~2.85s animation inside a 3.0s splash screen):
          0% -> Starts high above screen (-90vh)
          22% -> Drops like a falling ball and hits bottom floor (+23vh) with squash
          33% -> Rebounds up (-7vh)
          44% -> Second drop/bounce hitting bottom floor (+23vh)
          58% -> Settles smoothly into dead center (0, 0)
          72% - 94% -> Expands / blooms / unfolds ("লোগোটা মেলে গিয়ে") with outward scale
          100% -> Ready to enter main app
        */
        @keyframes logoBouncingDropSequence {
          0% {
            transform: translate3d(0, -95vh, 0) scale(0.7, 1.35) rotate(-18deg);
            opacity: 0.9;
          }
          18% {
            transform: translate3d(0, 15vh, 0) scale(0.85, 1.18) rotate(2deg);
          }
          22% {
            /* First floor hit: squash */
            transform: translate3d(0, 24vh, 0) scale(1.3, 0.7) rotate(0deg);
          }
          26% {
            /* Spring rebound */
            transform: translate3d(0, 16vh, 0) scale(0.9, 1.15) rotate(-3deg);
          }
          34% {
            /* Apex of first bounce */
            transform: translate3d(0, -6vh, 0) scale(1, 1) rotate(-1deg);
          }
          42% {
            /* Falling to second drop */
            transform: translate3d(0, 18vh, 0) scale(0.95, 1.08) rotate(1deg);
          }
          45% {
            /* Second floor hit (ড্রপ খাবে) */
            transform: translate3d(0, 24vh, 0) scale(1.18, 0.82) rotate(0deg);
          }
          49% {
            /* Soft rebound up towards center */
            transform: translate3d(0, 14vh, 0) scale(0.95, 1.05) rotate(0deg);
          }
          58% {
            /* Settled in center (মাঝখানে অবস্থান) */
            transform: translate3d(0, 0, 0) scale(1, 1) rotate(0deg);
          }
          72% {
            /* Breathing pause before bloom */
            transform: translate3d(0, 0, 0) scale(1, 1) rotate(0deg);
          }
          85% {
            /* মেলে গিয়ে: Blooms and expands outward gracefully */
            transform: translate3d(0, 0, 0) scale(1.25, 1.25) rotate(0deg);
            filter: drop-shadow(0 0 30px rgba(249, 115, 22, 0.65)) drop-shadow(0 0 60px rgba(14, 165, 233, 0.45));
          }
          96%, 100% {
            /* Fully bloomed majestic presence */
            transform: translate3d(0, 0, 0) scale(1.22, 1.22) rotate(0deg);
            filter: drop-shadow(0 0 35px rgba(249, 115, 22, 0.75)) drop-shadow(0 0 70px rgba(14, 165, 233, 0.55));
          }
        }

        /* Floor Shadow Synchronized with Ball Altitude */
        @keyframes floorShadowPulse {
          0% {
            transform: scale(0.2);
            opacity: 0.1;
          }
          20% {
            transform: scale(0.8);
            opacity: 0.6;
          }
          22% {
            /* Ball contacts floor */
            transform: scale(1.35);
            opacity: 0.9;
          }
          34% {
            /* Ball bounces high */
            transform: scale(0.5);
            opacity: 0.3;
          }
          45% {
            /* Ball second hit */
            transform: scale(1.2);
            opacity: 0.8;
          }
          58% {
            /* Ball moves to center */
            transform: scale(0.7);
            opacity: 0.35;
          }
          75%, 100% {
            transform: scale(0.85);
            opacity: 0.25;
          }
        }

        /* Shockwave rings on impacts */
        @keyframes shockwave1 {
          0%, 20% {
            transform: scale(0.4);
            opacity: 0;
          }
          22% {
            transform: scale(0.5);
            opacity: 0.9;
          }
          28% {
            transform: scale(1.6);
            opacity: 0;
          }
          100% {
            opacity: 0;
          }
        }

        @keyframes shockwave2 {
          0%, 43% {
            transform: scale(0.4);
            opacity: 0;
          }
          45% {
            transform: scale(0.5);
            opacity: 0.8;
          }
          52% {
            transform: scale(1.4);
            opacity: 0;
          }
          100% {
            opacity: 0;
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
