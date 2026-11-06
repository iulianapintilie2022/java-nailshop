const featuredCarousel = document.querySelector('#featured-carousel');

if (featuredCarousel) {
    const controls = document.querySelector('.carousel-controls');
    const previous = controls.querySelector('.carousel-previous');
    const next = controls.querySelector('.carousel-next');

    function updateCarouselControls() {
        const end = featuredCarousel.scrollWidth - featuredCarousel.clientWidth;
        controls.hidden = end <= 1;
        previous.disabled = featuredCarousel.scrollLeft <= 1;
        next.disabled = featuredCarousel.scrollLeft >= end - 1;
    }

    function moveFeaturedProducts(direction) {
        const card = featuredCarousel.querySelector('.product-card');
        const gap = parseFloat(getComputedStyle(featuredCarousel).columnGap) || 0;
        featuredCarousel.scrollBy({
            left: direction * (card.getBoundingClientRect().width + gap),
            behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth'
        });
    }

    previous.addEventListener('click', () => moveFeaturedProducts(-1));
    next.addEventListener('click', () => moveFeaturedProducts(1));
    featuredCarousel.addEventListener('scroll', updateCarouselControls, {passive: true});
    window.addEventListener('resize', updateCarouselControls);
    featuredCarousel.addEventListener('keydown', event => {
        if (event.target !== featuredCarousel) return;
        if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
            event.preventDefault();
            moveFeaturedProducts(event.key === 'ArrowLeft' ? -1 : 1);
        }
    });
    updateCarouselControls();
}
